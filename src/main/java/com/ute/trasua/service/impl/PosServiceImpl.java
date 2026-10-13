package com.ute.trasua.service.impl;

import com.ute.trasua.dto.pos.*;
import com.ute.trasua.entity.*;
import com.ute.trasua.repository.*;
import com.ute.trasua.service.PosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PosServiceImpl implements PosService {

    private final BranchRepository branchRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final SizeRepository sizeRepository;
    private final ToppingRepository toppingRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final EmployeeRepository employeeRepository;
    private final BranchMenuRepository branchMenuRepository;

    @org.springframework.beans.factory.annotation.Value("${app.vietqr.bank-bin:970422}")
    private String bankBin;

    @org.springframework.beans.factory.annotation.Value("${app.vietqr.account-no:0396202885}")
    private String accountNo;

    @org.springframework.beans.factory.annotation.Value("${app.vietqr.account-name:UTETRA TEA GALLERY}")
    private String accountName;

    @Override
    @Transactional(readOnly = true)
    public PosMenuResponse getPosMenu(Long branchId) {
        List<PosMenuResponse.CategoryDto> categories = categoryRepository.findAllByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(c -> PosMenuResponse.CategoryDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .description(c.getDescription())
                        .displayOrder(c.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        java.util.Map<Long, BranchMenu> branchMenuMap = new java.util.HashMap<>();
        if (branchId != null) {
            branchMenuRepository.findByBranchId(branchId).forEach(bm -> {
                if (bm.getProduct() != null) {
                    branchMenuMap.put(bm.getProduct().getId(), bm);
                }
            });
        }

        List<PosMenuResponse.ProductDto> products = productRepository.findByStatusOrderByNameAsc("ACTIVE")
                .stream()
                .filter(p -> {
                    if (branchId != null && branchMenuMap.containsKey(p.getId())) {
                        return branchMenuMap.get(p.getId()).isAvailable();
                    }
                    return true;
                })
                .map(p -> {
                    BigDecimal price = p.getBasePrice();
                    if (branchId != null && branchMenuMap.containsKey(p.getId())) {
                        BranchMenu bm = branchMenuMap.get(p.getId());
                        if (bm.getCustomPrice() != null) {
                            price = bm.getCustomPrice();
                        }
                    }
                    return PosMenuResponse.ProductDto.builder()
                            .id(p.getId())
                            .productCode(p.getProductCode())
                            .name(p.getName())
                            .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                            .categoryName(p.getCategory() != null ? p.getCategory().getName() : "")
                            .basePrice(price)
                            .imageUrl(p.getImageUrl())
                            .description(p.getDescription())
                            .status(p.getStatus())
                            .build();
                })
                .collect(Collectors.toList());

        List<PosMenuResponse.SizeDto> sizes = sizeRepository.findAllByActiveTrueOrderByExtraPriceAsc()
                .stream()
                .map(s -> PosMenuResponse.SizeDto.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .volumeMl(s.getVolumeMl())
                        .extraPrice(s.getExtraPrice())
                        .build())
                .collect(Collectors.toList());

        List<PosMenuResponse.ToppingDto> toppings = toppingRepository.findByStatusOrderByNameAsc("ACTIVE")
                .stream()
                .map(t -> PosMenuResponse.ToppingDto.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .price(t.getPrice())
                        .status(t.getStatus())
                        .build())
                .collect(Collectors.toList());

        return PosMenuResponse.builder()
                .categories(categories)
                .products(products)
                .sizes(sizes)
                .toppings(toppings)
                .build();
    }

    @Override
    @Transactional
    public PosOrderResponse createPosOrder(PosOrderRequest request) {
        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId()).orElse(null);
        }
        if (branch == null) {
            branch = branchRepository.findAll().stream().findFirst().orElse(null);
        }

        // Tính STT lấy nước trong ngày của chi nhánh
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        long countToday = (branch != null) ? orderRepository.countByBranchIdAndCreatedAtBetween(branch.getId(), startOfDay, endOfDay) : 0;
        int queueNumber = (int) countToday + 1;

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String orderCode = String.format("ORD-POS-%s-%04d", dateStr, queueNumber);

        Order order = Order.builder()
                .orderCode(orderCode)
                .dailyQueueNumber(queueNumber)
                .orderType("POS_WALK_IN")
                .status("PENDING")
                .paymentStatus("UNPAID")
                .cashierUsername(request.getCashierUsername() != null ? request.getCashierUsername() : "pos_cashier")
                .branch(branch)
                .note(request.getNote())
                .orderDetails(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        if (request.getItems() != null) {
            for (PosOrderItemRequest itemReq : request.getItems()) {
                Product product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + itemReq.getProductId()));

                Size size = null;
                if (itemReq.getSizeId() != null) {
                    size = sizeRepository.findById(itemReq.getSizeId()).orElse(null);
                }
                if (size == null) {
                    size = sizeRepository.findByName("M").orElseGet(() ->
                            sizeRepository.findAll().stream().findFirst().orElse(null));
                }

                BigDecimal unitPrice = product.getBasePrice();
                if (size != null && size.getExtraPrice() != null) {
                    unitPrice = unitPrice.add(size.getExtraPrice());
                }

                List<Topping> toppings = new ArrayList<>();
                if (itemReq.getToppingIds() != null && !itemReq.getToppingIds().isEmpty()) {
                    toppings = toppingRepository.findAllById(itemReq.getToppingIds());
                    for (Topping t : toppings) {
                        if (t.getPrice() != null) {
                            unitPrice = unitPrice.add(t.getPrice());
                        }
                    }
                }

                int qty = (itemReq.getQuantity() != null && itemReq.getQuantity() > 0) ? itemReq.getQuantity() : 1;
                BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(qty));
                totalAmount = totalAmount.add(itemSubtotal);

                OrderDetail detail = OrderDetail.builder()
                        .order(order)
                        .product(product)
                        .productName(product.getName())
                        .size(size)
                        .sizeName(size != null ? size.getName() : "M")
                        .quantity(qty)
                        .sugarLevel(itemReq.getSugarLevel() != null ? itemReq.getSugarLevel() : "100%")
                        .iceLevel(itemReq.getIceLevel() != null ? itemReq.getIceLevel() : "100%")
                        .unitPrice(unitPrice)
                        .subtotal(itemSubtotal)
                        .note(itemReq.getNote())
                        .toppings(new ArrayList<>())
                        .build();

                for (Topping t : toppings) {
                    OrderDetailTopping detailTopping = OrderDetailTopping.builder()
                            .orderDetail(detail)
                            .topping(t)
                            .toppingName(t.getName())
                            .price(t.getPrice())
                            .build();
                    detail.getToppings().add(detailTopping);
                }

                order.getOrderDetails().add(detail);
            }
        }

        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFinalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Sinh link ảnh VietQR QuickLink động theo chuẩn NAPAS
        String vietQrUrl = generateVietQrUrl(savedOrder.getOrderCode(), savedOrder.getFinalAmount());

        log.info(">>> Đã tạo đơn POS thành công: Mã {}, STT: #{}, Tổng tiền: {}₫",
                savedOrder.getOrderCode(), savedOrder.getDailyQueueNumber(), savedOrder.getFinalAmount());

        return mapToOrderResponse(savedOrder, vietQrUrl);
    }

    @Override
    @Transactional
    public PosOrderResponse processPosPayment(PosPaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + request.getOrderId()));

        String method = (request.getPaymentMethod() != null) ? request.getPaymentMethod() : "CASH";
        order.setPaymentMethod(method);
        order.setPaymentStatus("PAID");
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());

        BigDecimal amountPaid = request.getAmountPaid() != null ? request.getAmountPaid() : order.getFinalAmount();
        BigDecimal cashTendered = request.getCashTendered();
        BigDecimal changeAmount = request.getChangeAmount();

        if ("CASH".equalsIgnoreCase(method) && cashTendered != null) {
            if (changeAmount == null) {
                changeAmount = cashTendered.subtract(order.getFinalAmount());
                if (changeAmount.compareTo(BigDecimal.ZERO) < 0) {
                    changeAmount = BigDecimal.ZERO;
                }
            }
        }

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(method)
                .amount(amountPaid)
                .cashTendered(cashTendered)
                .changeAmount(changeAmount)
                .transactionCode(request.getTransactionCode() != null ? request.getTransactionCode() : "TXN-" + System.currentTimeMillis())
                .status("SUCCESS")
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        Order savedOrder = orderRepository.save(order);
        String vietQrUrl = generateVietQrUrl(savedOrder.getOrderCode(), savedOrder.getFinalAmount());

        log.info(">>> Đã xác nhận thanh toán POS thành công đơn {}: Phương thức {}, Số tiền: {}₫",
                savedOrder.getOrderCode(), method, amountPaid);

        return mapToOrderResponse(savedOrder, vietQrUrl);
    }

    @Override
    @Transactional
    public PosOrderResponse checkoutCashOrder(PosOrderRequest request, BigDecimal cashTendered, BigDecimal changeAmount) {
        // Tạo đơn hàng trước
        PosOrderResponse created = createPosOrder(request);
        Order order = orderRepository.findById(created.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + created.getOrderId()));

        order.setPaymentMethod("CASH");
        order.setPaymentStatus("PAID");
        order.setStatus("PROCESSING");

        BigDecimal amountPaid = order.getFinalAmount();
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod("CASH")
                .amount(amountPaid)
                .cashTendered(cashTendered != null ? cashTendered : amountPaid)
                .changeAmount(changeAmount != null ? changeAmount : BigDecimal.ZERO)
                .transactionCode("CASH-" + System.currentTimeMillis())
                .status("SUCCESS")
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        Order savedOrder = orderRepository.save(order);
        log.info(">>> Đã thanh toán TIỀN MẶT thành công đơn {}: Khách đưa {}₫, Tiền thối {}₫",
                savedOrder.getOrderCode(), cashTendered, changeAmount);

        return mapToOrderResponse(savedOrder, "");
    }

    @Override
    @Transactional
    public void cancelPendingOrder(Long orderId) {
        if (orderId == null) return;
        orderRepository.findById(orderId).ifPresent(order -> {
            if ("PENDING_PAYMENT".equalsIgnoreCase(order.getPaymentStatus()) || "PENDING".equalsIgnoreCase(order.getStatus())) {
                order.setStatus("CANCELLED");
                order.setPaymentStatus("CANCELLED");
                orderRepository.save(order);
                log.info(">>> Đã hủy đơn hàng treo ID: {} (Mã: {})", order.getId(), order.getOrderCode());
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PosOrderResponse getPosOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));
        String vietQrUrl = generateVietQrUrl(order.getOrderCode(), order.getFinalAmount());
        return mapToOrderResponse(order, vietQrUrl);
    }

    @Override
    @Transactional
    public List<ShiftEmployeeDto> getBranchEmployees(Long branchId) {
        Long targetBranchId = (branchId != null) ? branchId : 1L;
        Branch branch = branchRepository.findById(targetBranchId).orElse(null);
        if (branch != null) {
            if (!employeeRepository.existsByEmployeeCode("NV005")) {
                employeeRepository.save(Employee.builder()
                        .employeeCode("NV005")
                        .fullName("Phạm Gia Hân")
                        .phone("0955667788")
                        .status("ACTIVE")
                        .branch(branch)
                        .build());
            }
            if (!employeeRepository.existsByEmployeeCode("NV006")) {
                employeeRepository.save(Employee.builder()
                        .employeeCode("NV006")
                        .fullName("Đỗ Hoàng Long")
                        .phone("0966778899")
                        .status("ACTIVE")
                        .branch(branch)
                        .build());
            }
        }

        List<Employee> employees = employeeRepository.findByBranchIdAndStatus(targetBranchId, "ACTIVE");
        if (employees.isEmpty()) {
            // Fallback lấy toàn bộ nhân viên active nếu chi nhánh chưa có nhân viên riêng
            employees = employeeRepository.findAll().stream()
                    .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()))
                    .collect(Collectors.toList());
        }

        return employees.stream()
                .map(e -> ShiftEmployeeDto.builder()
                        .id(e.getId())
                        .employeeCode(e.getEmployeeCode())
                        .fullName(e.getFullName())
                        .phone(e.getPhone())
                        .activeOnShift(false)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftAttendanceResponse getShiftStatus(Long branchId, List<Long> activeEmployeeIds) {
        List<ShiftEmployeeDto> allEmployees = getBranchEmployees(branchId);
        List<ShiftEmployeeDto> onShiftList = new ArrayList<>();

        if (activeEmployeeIds != null && !activeEmployeeIds.isEmpty()) {
            for (ShiftEmployeeDto emp : allEmployees) {
                if (activeEmployeeIds.contains(emp.getId())) {
                    emp.setActiveOnShift(true);
                    onShiftList.add(emp);
                }
            }
        }

        boolean isActive = !onShiftList.isEmpty();
        String summary = onShiftList.stream()
                .map(ShiftEmployeeDto::getFullName)
                .collect(Collectors.joining(", "));

        return ShiftAttendanceResponse.builder()
                .shiftActive(isActive)
                .activeCount(onShiftList.size())
                .activeCashiersSummary(isActive ? summary : "Chưa có người trực ca")
                .activeEmployees(onShiftList)
                .build();
    }

    private String generateVietQrUrl(String orderCode, BigDecimal amount) {
        long amountLong = (amount != null) ? amount.longValue() : 0L;
        String encodedOrderCode = URLEncoder.encode(orderCode, StandardCharsets.UTF_8);
        String encodedName = URLEncoder.encode(accountName != null ? accountName : "UTETRA TEA GALLERY", StandardCharsets.UTF_8);
        return String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                bankBin != null ? bankBin : "970422",
                accountNo != null ? accountNo : "0396202885",
                amountLong, encodedOrderCode, encodedName);
    }

    private PosOrderResponse mapToOrderResponse(Order order, String vietQrUrl) {
        return PosOrderResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .dailyQueueNumber(order.getDailyQueueNumber())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .vietQrUrl(vietQrUrl)
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .cashierUsername(order.getCashierUsername())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
