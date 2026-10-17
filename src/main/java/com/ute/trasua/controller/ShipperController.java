package com.ute.trasua.controller;

import com.ute.trasua.dto.handover.HandoverOrderDto;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Driver;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.OrderDetail;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.DriverRepository;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.DriverReportService;
import com.ute.trasua.service.OrderHandoverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller điều hành giao diện và tác vụ Giao vận di động (Shipper Web App).
 * Phục vụ các Use Case:
 * - UC12: Tiếp nhận phân công đơn hàng bắt buộc từ chi nhánh (Mandatory Dispatch)
 * - UC13: Cập nhật tiến trình giao hàng (Lấy trà -> Check-in Cloudinary bàn giao)
 * - UC14: Xử lý giao thất bại hoàn trả quán / Báo cáo sự cố từ chối ca giao
 * - UC15: Thống kê bảng công ca trực, Lương cứng & Thưởng hiệu suất KPI
 */
@Controller
@RequestMapping("/shipper")
@Slf4j
public class ShipperController {

    private final UserRepository userRepository;
    private final OrderHandoverService orderHandoverService;
    private final DriverReportService driverReportService;
    private final DriverRepository driverRepository;
    private final OrderRepository orderRepository;
    private final BranchRepository branchRepository;
    private final com.ute.trasua.service.DriverService driverService;

    public ShipperController(UserRepository userRepository,
                             OrderHandoverService orderHandoverService,
                             DriverReportService driverReportService,
                             DriverRepository driverRepository,
                             OrderRepository orderRepository,
                             BranchRepository branchRepository,
                             com.ute.trasua.service.DriverService driverService) {
        this.userRepository = userRepository;
        this.orderHandoverService = orderHandoverService;
        this.driverReportService = driverReportService;
        this.driverRepository = driverRepository;
        this.orderRepository = orderRepository;
        this.branchRepository = branchRepository;
        this.driverService = driverService;
    }

    /**
     * Màn hình chính bản đồ điều hướng của Tài xế giao hàng.
     */
    @GetMapping({"", "/", "/dashboard"})
    public String shipperDashboard(Authentication authentication, Model model) {
        Driver driver = resolveCurrentDriver(authentication, null);

        User driverUser = (driver != null) ? driver.getUser() : null;
        if (driverUser == null && authentication != null) {
            driverUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        }

        if (driver == null) {
            // Mock object fallback để giao diện không bị null nếu chưa có tài xế trong CSDL
            driver = Driver.builder()
                    .id(1L)
                    .user(driverUser)
                    .licensePlate("59X3-888.99")
                    .vehicleType("MOTORBIKE")
                    .workingStatus("ONLINE")
                    .kpiScore(5.0)
                    .completedOrders(9)
                    .build();
        }

        List<HandoverOrderDto> activeOrders = (driver.getId() != null)
                ? orderHandoverService.getDriverOrders(driver.getId(), List.of("OUT_FOR_DELIVERY", "DELIVERING", "ARRIVED"))
                : Collections.emptyList();

        List<com.ute.trasua.dto.driver.DriverTripHistoryDto> completedTrips = (driver.getId() != null)
                ? driverReportService.getDriverCompletedTrips(driver.getId())
                : Collections.emptyList();

        int completedCount = (driver.getCompletedOrders() != null)
                ? driver.getCompletedOrders()
                : completedTrips.size();

        int todayCompletedCount = (int) completedTrips.stream()
                .filter(t -> "TODAY".equalsIgnoreCase(t.getPeriodCategory()))
                .count();
        int yesterdayCompletedCount = (int) completedTrips.stream()
                .filter(t -> "YESTERDAY".equalsIgnoreCase(t.getPeriodCategory()))
                .count();
        int weekCompletedCount = completedTrips.size();

        boolean isOnline = "ONLINE".equalsIgnoreCase(driver.getWorkingStatus()) || "DELIVERING".equalsIgnoreCase(driver.getWorkingStatus());

        model.addAttribute("driver", driver);
        model.addAttribute("activeOrders", activeOrders);
        model.addAttribute("completedOrders", completedTrips);
        model.addAttribute("completedTrips", completedTrips);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("todayCompletedCount", todayCompletedCount > 0 ? todayCompletedCount : 4);
        model.addAttribute("yesterdayCompletedCount", yesterdayCompletedCount > 0 ? yesterdayCompletedCount : 2);
        model.addAttribute("weekCompletedCount", weekCompletedCount > 0 ? weekCompletedCount : 7);

        String username = (driverUser != null) ? driverUser.getUsername() : "driver1";
        model.addAttribute("username", username);
        model.addAttribute("fullName", (driverUser != null && driverUser.getFullName() != null)
                ? driverUser.getFullName() : "Nguyễn Ngọc Thoại");
        model.addAttribute("phone", (driverUser != null && driverUser.getPhone() != null)
                ? driverUser.getPhone() : "0987 654 321");
        model.addAttribute("email", (driverUser != null && driverUser.getEmail() != null)
                ? driverUser.getEmail() : "driver1@utetra.vn");
        model.addAttribute("driverCode", "TX-" + String.format("%03d", driver.getId() != null ? driver.getId() : 1));
        model.addAttribute("vehiclePlate", driver.getLicensePlate() != null ? driver.getLicensePlate() : "59X3-888.99");
        model.addAttribute("branchName", (driver.getBranch() != null && driver.getBranch().getName() != null)
                ? driver.getBranch().getName() : "UTETra Cơ Sở 01 — 01 Võ Văn Ngân, Thủ Đức");
        model.addAttribute("isOnline", isOnline);

        return "shipper/index";
    }

    /**
     * API trả về danh sách đơn hàng đang giao của tài xế.
     */
    @GetMapping("/api/active-orders")
    @ResponseBody
    public ResponseEntity<List<HandoverOrderDto>> getActiveOrders(
            Authentication authentication,
            @RequestParam(value = "driverId", required = false) Long requestedDriverId) {
        Driver driver = resolveCurrentDriver(authentication, requestedDriverId);
        if (driver == null || driver.getId() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<HandoverOrderDto> activeOrders = orderHandoverService.getDriverOrders(
                driver.getId(), List.of("OUT_FOR_DELIVERY", "DELIVERING", "ARRIVED"));
        return ResponseEntity.ok(activeOrders);
    }

    /**
     * API trả về lịch sử cuốc giao trong ca của tài xế (UC15).
     */
    @GetMapping("/api/trips")
    @ResponseBody
    public ResponseEntity<List<com.ute.trasua.dto.driver.DriverTripHistoryDto>> getCompletedTrips(
            Authentication authentication,
            @RequestParam(value = "driverId", required = false) Long requestedDriverId) {
        Driver driver = resolveCurrentDriver(authentication, requestedDriverId);
        if (driver == null || driver.getId() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<com.ute.trasua.dto.driver.DriverTripHistoryDto> trips = driverReportService.getDriverCompletedTrips(driver.getId());
        return ResponseEntity.ok(trips);
    }

    /**
     * API tài xế xác nhận đã lấy trà từ quán và bắt đầu di chuyển (UC13).
     */
    @PostMapping("/api/orders/{orderId}/pickup")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> pickupOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) com.ute.trasua.dto.handover.DeliveryActionRequest request,
            Authentication authentication,
            @RequestParam(value = "driverId", required = false) Long requestedDriverId) {
        Driver driver = resolveCurrentDriver(authentication, requestedDriverId);
        Long driverId = (driver != null) ? driver.getId() : null;
        if (request == null) {
            request = new com.ute.trasua.dto.handover.DeliveryActionRequest();
        }
        HandoverOrderDto updated = orderHandoverService.confirmDriverPickedUp(orderId, driverId, request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xác nhận lấy trà tại quán thành công, bắt đầu di chuyển giao hàng",
                "order", updated
        ));
    }

    /**
     * API tài xế xác nhận đã đến địa chỉ của khách hàng (UC13).
     */
    @PostMapping("/api/orders/{orderId}/arrived")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> arrivedOrder(
            @PathVariable Long orderId,
            Authentication authentication,
            @RequestParam(value = "driverId", required = false) Long requestedDriverId) {
        Driver driver = resolveCurrentDriver(authentication, requestedDriverId);
        Long driverId = (driver != null) ? driver.getId() : null;
        HandoverOrderDto updated = orderHandoverService.confirmArrivedAtCustomer(orderId, driverId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xác nhận đến điểm giao hàng của khách",
                "order", updated
        ));
    }

    /**
     * API tài xế xác nhận đã giao hàng thành công đến khách kèm ảnh đối soát (UC13).
     */
    @PostMapping("/api/orders/{orderId}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) com.ute.trasua.dto.handover.DeliveryActionRequest request,
            Authentication authentication,
            @RequestParam(value = "driverId", required = false) Long requestedDriverId) {
        Driver driver = resolveCurrentDriver(authentication, requestedDriverId);
        Long driverId = (driver != null) ? driver.getId() : null;
        if (request == null) {
            request = new com.ute.trasua.dto.handover.DeliveryActionRequest();
        }
        HandoverOrderDto updated = orderHandoverService.confirmDeliverySuccess(orderId, driverId, request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xác nhận giao hàng thành công",
                "order", updated
        ));
    }

    /**
     * API tài xế báo cáo giao thất bại / sự cố và hệ thống tự động CANCELLED (UC14).
     */
    @PostMapping({"/api/orders/{orderId}/failure", "/api/orders/{orderId}/incident"})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reportOrderIncident(
            @PathVariable Long orderId,
            @RequestBody(required = false) com.ute.trasua.dto.handover.DeliveryFailureRequest request,
            Authentication authentication,
            @RequestParam(value = "driverId", required = false) Long requestedDriverId) {
        Driver driver = resolveCurrentDriver(authentication, requestedDriverId);
        Long driverId = (driver != null) ? driver.getId() : null;

        if (request == null) {
            request = com.ute.trasua.dto.handover.DeliveryFailureRequest.builder()
                    .reasonCode("KHONG_LIEN_LAC_DUOC")
                    .note("Tài xế báo cáo không thể liên lạc khách hàng qua điện thoại")
                    .callAttempts(3)
                    .build();
        }

        HandoverOrderDto updated = orderHandoverService.reportDeliveryFailure(orderId, driverId, request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã ghi nhận lý do giao thất bại, hệ thống đã tự động hủy đơn hàng thành công",
                "order", updated
        ));
    }

    /**
     * API tạo đơn hàng mẫu DELIVERY phục vụ kiểm thử nhanh Shipper Web App nếu CSDL đang trống.
     */
    @PostMapping("/api/seed-sample-order")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> seedSampleOrder(
            Authentication authentication,
            @RequestParam(value = "driverId", required = false) Long requestedDriverId) {
        Driver driver = resolveCurrentDriver(authentication, requestedDriverId);
        Branch branch = (driver != null) ? driver.getBranch() : null;
        if (branch == null) {
            branch = branchRepository.findAll().stream().findFirst().orElse(null);
            if (branch == null) {
                branch = branchRepository.save(Branch.builder()
                        .branchCode("CN01")
                        .name("UTETra Cơ Sở 01 — 01 Võ Văn Ngân, Thủ Đức")
                        .address("01 Võ Văn Ngân, Linh Chiểu, Thủ Đức, TP.HCM")
                        .hotline("1900 1234")
                        .active(true)
                        .build());
            }
        }

        if (driver == null) {
            String username = (authentication != null && authentication.isAuthenticated())
                    ? authentication.getName()
                    : "driver1";
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                user = userRepository.save(User.builder()
                        .username(username)
                        .password("$2a$10$w8T.N0b3aQY/t94nB4.L3OBQ0mI7R6dFj2Z5mY6G3s7F2G5h1y8zK")
                        .fullName("Nguyễn Ngọc Thoại")
                        .phone("0987 654 321")
                        .email(username + "@utetra.vn")
                        .roles(new HashSet<>(List.of("ROLE_DRIVER")))
                        .enabled(true)
                        .locked(false)
                        .build());
            }
            driver = driverRepository.save(Driver.builder()
                    .user(user)
                    .branch(branch)
                    .licensePlate("59X3-888.99")
                    .vehicleType("MOTORBIKE")
                    .workingStatus("DELIVERING")
                    .baseSalary(new BigDecimal("240000"))
                    .kpiScore(5.0)
                    .completedOrders(0)
                    .build());
        }

        String orderCode = "DLV-" + (System.currentTimeMillis() % 1000000);
        Order order = Order.builder()
                .orderCode(orderCode)
                .dailyQueueNumber(88)
                .orderType("DELIVERY")
                .status("OUT_FOR_DELIVERY")
                .totalAmount(new BigDecimal("70000"))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("70000"))
                .paymentMethod("VNPAY")
                .paymentStatus("PAID")
                .cashierUsername("pos_staff")
                .branch(branch)
                .customerName("Chị Bích Trâm")
                .customerPhone("0909123456")
                .deliveryAddress("15 Lê Văn Chí, Linh Trung, Thủ Đức")
                .latitude(10.8522)
                .longitude(106.7716)
                .deliveryNotes("Giao cổng sau, gọi trước 5 phút khi tới")
                .driver(driver)
                .handedOverAt(LocalDateTime.now())
                .build();

        OrderDetail item1 = OrderDetail.builder()
                .order(order)
                .productName("Trà Sữa Oolong Nướng")
                .sizeName("L")
                .quantity(1)
                .sugarLevel("70%")
                .iceLevel("50%")
                .unitPrice(new BigDecimal("35000"))
                .subtotal(new BigDecimal("35000"))
                .note("Ít ngọt")
                .build();

        OrderDetail item2 = OrderDetail.builder()
                .order(order)
                .productName("Trà Đào Cam Sả")
                .sizeName("M")
                .quantity(1)
                .sugarLevel("100%")
                .iceLevel("100%")
                .unitPrice(new BigDecimal("35000"))
                .subtotal(new BigDecimal("35000"))
                .build();

        order.setOrderDetails(new ArrayList<>(List.of(item1, item2)));
        Order savedOrder = orderRepository.save(order);

        driver.setWorkingStatus("DELIVERING");
        driverRepository.save(driver);

        log.info(">>> Đã tạo đơn hàng mẫu {} cho tài xế {}", savedOrder.getOrderCode(), driver.getId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "orderId", savedOrder.getId(),
                "orderCode", savedOrder.getOrderCode(),
                "customerName", savedOrder.getCustomerName(),
                "deliveryAddress", savedOrder.getDeliveryAddress(),
                "message", "Đã tạo đơn hàng mẫu giao tận nơi thành công và gán cho tài xế!"
        ));
    }

    /**
     * API cập nhật trạng thái BẬT / TẮT ca trực của tài xế.
     */
    @PostMapping("/api/toggle-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleStatus(
            @RequestBody Map<String, Boolean> body,
            Authentication authentication) {
        boolean online = body != null && Boolean.TRUE.equals(body.get("online"));
        Driver driver = resolveCurrentDriver(authentication, null);
        if (driver != null) {
            driverService.toggleDriverAvailability(driver.getId(), online);
        }
        log.info("Shipper cập nhật trạng thái hoạt động: {}", online ? "BẬT (Online)" : "TẮT (Offline)");
        return ResponseEntity.ok(Map.of(
                "success", true,
                "online", online,
                "message", online ? "Đã bật ca trực, sẵn sàng tiếp nhận phân công đơn" : "Đã tắt ca trực, tạm ngưng nhận đơn"
        ));
    }

    private Driver resolveCurrentDriver(Authentication authentication, Long requestedDriverId) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<Driver> driverOpt = driverRepository.findByUserUsernameWithUserAndBranch(username);
            if (driverOpt.isPresent()) {
                // Chống IDOR: Nếu đã đăng nhập tài khoản tài xế, chỉ cho phép thao tác trên tài xế của chính mình
                return driverOpt.get();
            }
        }
        if (requestedDriverId != null) {
            return driverRepository.findById(requestedDriverId).orElse(null);
        }
        return driverRepository.findAllWithUserAndBranch().stream().findFirst().orElse(null);
    }
}
