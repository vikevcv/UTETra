package com.ute.trasua.service.impl;

import com.ute.trasua.dto.UserResponse;
import com.ute.trasua.dto.UserSearchRequest;
import com.ute.trasua.dto.UserUpdateRequest;
import com.ute.trasua.dto.user.UserDriverTripDto;
import com.ute.trasua.dto.user.UserOrderHistoryDto;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.OrderDetail;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation xử lý nghiệp vụ quản lý người dùng và giám sát tài xế toàn chuỗi (Module 11 - UC20, UC25).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final com.ute.trasua.repository.BranchRepository branchRepository;
    private final com.ute.trasua.repository.PosAccountRepository posAccountRepository;
    private final com.ute.trasua.repository.DriverRepository driverRepository;
    private final OrderRepository orderRepository;

    private static final DecimalFormat CURRENCY_FMT = new DecimalFormat("#,### đ");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(UserSearchRequest request) {
        int page = Math.max(0, request.getPage());
        int size = request.getSize() > 0 ? request.getSize() : 10;
        
        Sort.Direction direction = "asc".equalsIgnoreCase(request.getSortDir()) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        
        String sortBy = (request.getSortBy() != null && !request.getSortBy().isBlank()) 
                ? request.getSortBy() 
                : "id";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        String keyword = (request.getKeyword() != null) ? request.getKeyword().trim() : null;
        String role = (request.getRole() != null && !request.getRole().isBlank()) ? request.getRole().trim() : null;
        Boolean locked = request.getLocked();

        Page<User> usersPage = userRepository.searchUsers(keyword, role, locked, pageable);
        return usersPage.map(UserResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public void toggleLock(Long id, boolean lock, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));

        // Bảo vệ: Không cho phép khóa tài khoản ADMIN tối cao
        if (lock && user.getRoles().contains("ROLE_ADMIN")) {
            throw new IllegalStateException("Không được phép khóa tài khoản Quản trị viên (ADMIN) cấp cao!");
        }

        user.setLocked(lock);
        if (lock) {
            user.setLockReason(reason != null && !reason.isBlank() 
                    ? reason 
                    : "Tài khoản bị khóa bởi Quản trị viên");
            log.info("User {} (ID: {}) bị khóa với lý do: {}", user.getUsername(), id, user.getLockReason());
        } else {
            user.setLockReason(null);
            log.info("User {} (ID: {}) đã được mở khóa", user.getUsername(), id);
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar().trim());
        }

        // Cập nhật vai trò (roles) và chi nhánh trực thuộc
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<String> newRoles = new HashSet<>(request.getRoles());
            boolean requiresBranch = newRoles.contains("ROLE_MANAGER") 
                    || newRoles.contains("ROLE_STAFF") 
                    || newRoles.contains("ROLE_DRIVER");

            if (requiresBranch) {
                Long branchId = request.getBranchId();
                if (branchId == null) {
                    throw new IllegalArgumentException("Vui lòng chỉ định Chi nhánh trực thuộc khi phân quyền Quản lý, Nhân viên hoặc Tài xế!");
                }
                com.ute.trasua.entity.Branch branch = branchRepository.findById(branchId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh với ID: " + branchId));

                user.setRoles(newRoles);
                user.setBranch(branch);

                if (newRoles.contains("ROLE_MANAGER")) {
                    // Gỡ quyền manager ở các chi nhánh cũ khác
                    java.util.List<com.ute.trasua.entity.Branch> oldBranches = branchRepository.findByManager(user);
                    for (com.ute.trasua.entity.Branch oldB : oldBranches) {
                        if (!oldB.getId().equals(branch.getId())) {
                            oldB.setManager(null);
                            branchRepository.save(oldB);
                        }
                    }
                    branch.setManager(user);
                    branchRepository.save(branch);
                } else {
                    // Nếu không còn là manager, gỡ khỏi branch.manager nếu có
                    java.util.List<com.ute.trasua.entity.Branch> managedBranches = branchRepository.findByManager(user);
                    for (com.ute.trasua.entity.Branch b : managedBranches) {
                        b.setManager(null);
                        branchRepository.save(b);
                    }
                }

                if (newRoles.contains("ROLE_STAFF")) {
                    com.ute.trasua.entity.PosAccount posAcc = posAccountRepository.findByUserId(user.getId())
                            .or(() -> posAccountRepository.findByUsername(user.getUsername()))
                            .orElse(null);
                    if (posAcc != null) {
                        posAcc.setBranch(branch);
                        posAcc.setUser(user);
                        posAcc.setActive(true);
                        posAccountRepository.save(posAcc);
                    } else {
                        com.ute.trasua.entity.PosAccount newPos = com.ute.trasua.entity.PosAccount.builder()
                                .username(user.getUsername())
                                .counterName(user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : (user.getUsername() + " (Quầy POS)"))
                                .password(user.getPassword() != null ? user.getPassword() : "123456")
                                .branch(branch)
                                .user(user)
                                .active(true)
                                .note("Phân quyền nhân viên chi nhánh " + branch.getName())
                                .build();
                        posAccountRepository.save(newPos);
                    }
                }

                if (newRoles.contains("ROLE_DRIVER")) {
                    com.ute.trasua.entity.Driver driver = driverRepository.findByUserId(user.getId()).orElse(null);
                    if (driver != null) {
                        driver.setBranch(branch);
                        driverRepository.save(driver);
                    } else {
                        String plate = "59-T1 " + (10000 + (int) (Math.random() * 89999));
                        com.ute.trasua.entity.Driver newDriver = com.ute.trasua.entity.Driver.builder()
                                .user(user)
                                .branch(branch)
                                .licensePlate(plate)
                                .vehicleType("MOTORBIKE")
                                .workingStatus("ONLINE")
                                .baseSalary(new java.math.BigDecimal("5000000"))
                                .kpiScore(5.0)
                                .completedOrders(0)
                                .build();
                        driverRepository.save(newDriver);
                    }
                }
            } else {
                // Khách hàng hoặc Admin toàn chuỗi: không gán chi nhánh cố định
                user.setRoles(newRoles);
                user.setBranch(null);
                java.util.List<com.ute.trasua.entity.Branch> managedBranches = branchRepository.findByManager(user);
                for (com.ute.trasua.entity.Branch b : managedBranches) {
                    b.setManager(null);
                    branchRepository.save(b);
                }
            }
        } else if (request.getBranchId() != null) {
            // Trường hợp không đổi role nhưng cập nhật chi nhánh
            com.ute.trasua.entity.Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh với ID: " + request.getBranchId()));
            user.setBranch(branch);
            if (user.getRoles() != null && user.getRoles().contains("ROLE_MANAGER")) {
                java.util.List<com.ute.trasua.entity.Branch> oldBranches = branchRepository.findByManager(user);
                for (com.ute.trasua.entity.Branch oldB : oldBranches) {
                    if (!oldB.getId().equals(branch.getId())) {
                        oldB.setManager(null);
                        branchRepository.save(oldB);
                    }
                }
                branch.setManager(user);
                branchRepository.save(branch);
            }
        }

        // Cập nhật trạng thái khóa nếu có thay đổi
        if (request.getLocked() != null && request.getLocked() != user.isLocked()) {
            toggleLock(id, request.getLocked(), request.getLockReason());
        } else {
            userRepository.save(user);
        }

        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchDrivers(String keyword, int page, int size) {
        int pageIndex = Math.max(0, page);
        int pageSize = size > 0 ? size : 10;
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        
        String cleanKeyword = (keyword != null) ? keyword.trim() : null;
        Page<User> driverPage = userRepository.searchDrivers(cleanKeyword, pageable);
        return driverPage.map(UserResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics() {
        long totalUsers = userRepository.count();
        long lockedUsers = userRepository.countByLocked(true);
        long activeUsers = totalUsers - lockedUsers;
        long totalDrivers = userRepository.countByRole("ROLE_DRIVER");
        long totalCustomers = userRepository.countByRole("ROLE_CUSTOMER");
        long totalStaff = userRepository.countByRole("ROLE_STAFF") + userRepository.countByRole("ROLE_MANAGER");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("lockedUsers", lockedUsers);
        stats.put("totalDrivers", totalDrivers);
        stats.put("totalCustomers", totalCustomers);
        stats.put("totalStaff", totalStaff);
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserOrderHistoryDto> getUserOrderHistory(Long userId) {
        if (userId == null) return Collections.emptyList();
        List<Order> orders = orderRepository.findAllByUserIdWithDetailsOrderByCreatedAtDesc(userId);
        List<UserOrderHistoryDto> result = new ArrayList<>();

        for (Order o : orders) {
            int totalQty = 0;
            List<String> itemNames = new ArrayList<>();
            List<UserOrderHistoryDto.OrderItemDetailDto> items = new ArrayList<>();

            if (o.getOrderDetails() != null) {
                for (OrderDetail d : o.getOrderDetails()) {
                    int qty = (d.getQuantity() != null) ? d.getQuantity() : 0;
                    totalQty += qty;
                    String pName = (d.getProductName() != null) ? d.getProductName() : "Món nước";
                    itemNames.add(pName + " (x" + qty + ")");

                    String topSummary = "";
                    if (d.getToppings() != null && !d.getToppings().isEmpty()) {
                        topSummary = d.getToppings().stream()
                                .map(com.ute.trasua.entity.OrderDetailTopping::getToppingName)
                                .filter(Objects::nonNull)
                                .collect(Collectors.joining(", "));
                    }

                    BigDecimal sub = (d.getSubtotal() != null) ? d.getSubtotal() : BigDecimal.ZERO;
                    items.add(UserOrderHistoryDto.OrderItemDetailDto.builder()
                            .productName(pName)
                            .sizeName(d.getSizeName())
                            .quantity(qty)
                            .subtotal(sub)
                            .formattedSubtotal(CURRENCY_FMT.format(sub))
                            .toppingsSummary(topSummary)
                            .build());
                }
            }

            String summaryItems = itemNames.isEmpty() ? "Không có món" : String.join(", ", itemNames);
            if (summaryItems.length() > 60) {
                summaryItems = summaryItems.substring(0, 57) + "...";
            }

            BigDecimal tot = (o.getTotalAmount() != null) ? o.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal disc = (o.getDiscountAmount() != null) ? o.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal ship = (o.getShippingFee() != null) ? o.getShippingFee() : BigDecimal.ZERO;
            BigDecimal fin = (o.getFinalAmount() != null) ? o.getFinalAmount() : BigDecimal.ZERO;

            String driverName = "";
            String driverPhone = "";
            if (o.getDriver() != null && o.getDriver().getUser() != null) {
                driverName = o.getDriver().getUser().getFullName();
                driverPhone = o.getDriver().getUser().getPhone();
            }

            result.add(UserOrderHistoryDto.builder()
                    .orderId(o.getId())
                    .orderCode(o.getOrderCode())
                    .branchName(o.getBranch() != null ? o.getBranch().getName() : "Toàn chuỗi")
                    .branchCode(o.getBranch() != null ? o.getBranch().getBranchCode() : "")
                    .createdAt(o.getCreatedAt())
                    .formattedCreatedAt(o.getCreatedAt() != null ? o.getCreatedAt().format(DATE_TIME_FMT) : "")
                    .totalAmount(tot)
                    .formattedTotalAmount(CURRENCY_FMT.format(tot))
                    .discountAmount(disc)
                    .formattedDiscountAmount(CURRENCY_FMT.format(disc))
                    .shippingFee(ship)
                    .formattedShippingFee(CURRENCY_FMT.format(ship))
                    .finalAmount(fin)
                    .formattedFinalAmount(CURRENCY_FMT.format(fin))
                    .orderType(o.getOrderType())
                    .orderTypeDisplay(mapOrderType(o.getOrderType()))
                    .paymentMethod(o.getPaymentMethod())
                    .paymentMethodDisplay(mapPaymentMethod(o.getPaymentMethod()))
                    .paymentStatus(o.getPaymentStatus())
                    .status(o.getStatus())
                    .statusDisplay(mapOrderStatus(o.getStatus()))
                    .statusBadgeClass(mapOrderStatusBadge(o.getStatus()))
                    .driverName(driverName)
                    .driverPhone(driverPhone)
                    .deliveryAddress(o.getDeliveryAddress())
                    .totalQuantity(totalQty)
                    .itemsSummary(summaryItems)
                    .items(items)
                    .build());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserOrderSummary(Long userId) {
        List<Order> orders = orderRepository.findAllByUserIdWithDetailsOrderByCreatedAtDesc(userId);
        long totalOrders = orders.size();
        long completedOrders = 0;
        long cancelledOrders = 0;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Order o : orders) {
            String s = (o.getStatus() != null) ? o.getStatus().toUpperCase() : "";
            if ("COMPLETED".equals(s) || "DELIVERED".equals(s) || "PAID".equals(s) || "PAID".equalsIgnoreCase(o.getPaymentStatus())) {
                completedOrders++;
                if (o.getFinalAmount() != null) {
                    totalSpent = totalSpent.add(o.getFinalAmount());
                }
            } else if ("CANCELLED".equals(s)) {
                cancelledOrders++;
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOrders", totalOrders);
        summary.put("completedOrders", completedOrders);
        summary.put("cancelledOrders", cancelledOrders);
        summary.put("totalSpent", totalSpent);
        summary.put("formattedTotalSpent", CURRENCY_FMT.format(totalSpent));
        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDriverTripDto> getDriverTripHistory(Long userId) {
        if (userId == null) return Collections.emptyList();
        com.ute.trasua.entity.Driver driver = driverRepository.findByUserId(userId).orElse(null);
        if (driver == null) return Collections.emptyList();

        List<Order> orders = orderRepository.findAllByDriverIdWithDetailsOrderByCreatedAtDesc(driver.getId());
        List<UserDriverTripDto> result = new ArrayList<>();

        for (Order o : orders) {
            int cupCount = 0;
            if (o.getOrderDetails() != null) {
                cupCount = o.getOrderDetails().stream()
                        .mapToInt(d -> d.getQuantity() != null ? d.getQuantity() : 0)
                        .sum();
            }

            BigDecimal ship = (o.getShippingFee() != null) ? o.getShippingFee() : BigDecimal.ZERO;
            BigDecimal fin = (o.getFinalAmount() != null) ? o.getFinalAmount() : BigDecimal.ZERO;

            String customerName = o.getCustomerName();
            String customerPhone = o.getCustomerPhone();
            if ((customerName == null || customerName.isBlank()) && o.getUser() != null) {
                customerName = o.getUser().getFullName();
                customerPhone = o.getUser().getPhone();
            }
            if (customerName == null || customerName.isBlank()) {
                customerName = "Khách hàng";
            }

            result.add(UserDriverTripDto.builder()
                    .orderId(o.getId())
                    .orderCode(o.getOrderCode())
                    .branchName(o.getBranch() != null ? o.getBranch().getName() : "")
                    .branchCode(o.getBranch() != null ? o.getBranch().getBranchCode() : "")
                    .customerName(customerName)
                    .customerPhone(customerPhone)
                    .deliveryAddress(o.getDeliveryAddress())
                    .distanceKm(o.getDistanceKm())
                    .cupCount(cupCount)
                    .shippingFee(ship)
                    .formattedShippingFee(CURRENCY_FMT.format(ship))
                    .finalAmount(fin)
                    .formattedFinalAmount(CURRENCY_FMT.format(fin))
                    .status(o.getStatus())
                    .statusDisplay(mapOrderStatus(o.getStatus()))
                    .statusBadgeClass(mapOrderStatusBadge(o.getStatus()))
                    .createdAt(o.getCreatedAt())
                    .formattedCreatedAt(o.getCreatedAt() != null ? o.getCreatedAt().format(DATE_TIME_FMT) : "")
                    .handedOverAt(o.getHandedOverAt())
                    .formattedHandedOverAt(o.getHandedOverAt() != null ? o.getHandedOverAt().format(DATE_TIME_FMT) : "")
                    .deliveredAt(o.getDeliveredAt())
                    .formattedDeliveredAt(o.getDeliveredAt() != null ? o.getDeliveredAt().format(DATE_TIME_FMT) : "")
                    .pickupPhotoUrl(o.getPickupPhotoUrl())
                    .deliveryPhotoUrl(o.getDeliveryPhotoUrl())
                    .incidentReason(o.getIncidentReason())
                    .incidentNote(o.getIncidentNote())
                    .failureReason(o.getFailureReason())
                    .failureProofUrl(o.getFailureProofUrl())
                    .build());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDriverTripSummary(Long userId) {
        Map<String, Object> summary = new HashMap<>();
        com.ute.trasua.entity.Driver driver = driverRepository.findByUserId(userId).orElse(null);
        if (driver == null) {
            summary.put("isDriver", false);
            return summary;
        }

        List<Order> orders = orderRepository.findAllByDriverIdWithDetailsOrderByCreatedAtDesc(driver.getId());
        long totalTrips = orders.size();
        long completedTrips = 0;
        long deliveringTrips = 0;
        long failedTrips = 0;
        BigDecimal totalDeliveryFee = BigDecimal.ZERO;

        for (Order o : orders) {
            String s = (o.getStatus() != null) ? o.getStatus().toUpperCase() : "";
            if ("COMPLETED".equals(s) || "DELIVERED".equals(s)) {
                completedTrips++;
                if (o.getShippingFee() != null) {
                    totalDeliveryFee = totalDeliveryFee.add(o.getShippingFee());
                }
            } else if ("DELIVERING".equals(s)) {
                deliveringTrips++;
            } else if ("DELIVERY_FAILED".equals(s) || "FAILED".equals(s) || "CANCELLED".equals(s)) {
                failedTrips++;
            }
        }

        summary.put("isDriver", true);
        summary.put("driverId", driver.getId());
        summary.put("licensePlate", driver.getLicensePlate());
        summary.put("vehicleType", driver.getVehicleType());
        summary.put("workingStatus", driver.getWorkingStatus());
        summary.put("kpiScore", driver.getKpiScore() != null ? driver.getKpiScore() : 5.0);
        summary.put("totalTrips", totalTrips);
        summary.put("completedTrips", completedTrips);
        summary.put("deliveringTrips", deliveringTrips);
        summary.put("failedTrips", failedTrips);
        summary.put("totalDeliveryFee", totalDeliveryFee);
        summary.put("formattedTotalDeliveryFee", CURRENCY_FMT.format(totalDeliveryFee));
        return summary;
    }

    private String mapOrderType(String type) {
        if (type == null) return "Đặt hàng";
        return switch (type.toUpperCase()) {
            case "ONLINE_DELIVERY" -> "Giao hàng tận nơi";
            case "POS_WALK_IN" -> "Tại quầy (POS)";
            case "TAKEAWAY" -> "Mang đi";
            default -> type;
        };
    }

    private String mapPaymentMethod(String pm) {
        if (pm == null) return "Chưa xác định";
        return switch (pm.toUpperCase()) {
            case "CASH" -> "Tiền mặt";
            case "VIETQR", "BANK_TRANSFER" -> "Chuyển khoản VietQR";
            case "MOMO" -> "Ví MoMo";
            case "VNPAY" -> "Cổng VNPAY";
            default -> pm;
        };
    }

    private String mapOrderStatus(String status) {
        if (status == null) return "Đang xử lý";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "PREPARING" -> "Đang pha chế";
            case "READY_FOR_DELIVERY" -> "Chờ tài xế lấy";
            case "DELIVERING" -> "Đang giao hàng";
            case "DELIVERED", "COMPLETED", "PAID" -> "Hoàn tất";
            case "CANCELLED" -> "Đã hủy";
            case "DELIVERY_FAILED" -> "Giao thất bại";
            default -> status;
        };
    }

    private String mapOrderStatusBadge(String status) {
        if (status == null) return "zen-badge-secondary";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "zen-badge-warning";
            case "CONFIRMED", "PREPARING" -> "zen-badge-info";
            case "READY_FOR_DELIVERY", "DELIVERING" -> "zen-badge-primary";
            case "DELIVERED", "COMPLETED", "PAID" -> "zen-badge-success";
            case "CANCELLED", "DELIVERY_FAILED" -> "zen-badge-danger";
            default -> "zen-badge-secondary";
        };
    }
}
