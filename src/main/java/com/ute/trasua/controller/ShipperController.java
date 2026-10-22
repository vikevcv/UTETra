package com.ute.trasua.controller;

import com.ute.trasua.dto.handover.HandoverOrderDto;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Driver;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.User;
import com.ute.trasua.service.DriverReportService;
import com.ute.trasua.service.DriverService;
import com.ute.trasua.service.OrderHandoverService;
import com.ute.trasua.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private final UserService userService;
    private final OrderHandoverService orderHandoverService;
    private final DriverReportService driverReportService;
    private final DriverService driverService;

    public ShipperController(UserService userService,
                             OrderHandoverService orderHandoverService,
                             DriverReportService driverReportService,
                             DriverService driverService) {
        this.userService = userService;
        this.orderHandoverService = orderHandoverService;
        this.driverReportService = driverReportService;
        this.driverService = driverService;
    }

    /**
     * Màn hình chính bản đồ điều hướng của Tài xế giao hàng.
     */
    @GetMapping({"", "/", "/dashboard"})
    public String shipperDashboard(Authentication authentication, Model model) {
        Driver driver = resolveCurrentDriver(authentication, null);
        if (!isOwnDriver(authentication, driver)) {
            return "redirect:/?accessDenied=true";
        }

        User driverUser = (driver != null) ? driver.getUser() : null;
        if (driverUser == null && authentication != null) {
            driverUser = userService.findByUsername(authentication.getName()).orElse(null);
        }

        if (driver == null) {
            // Trường hợp tài khoản chưa được liên kết hồ sơ tài xế trong CSDL
            driver = Driver.builder()
                    .user(driverUser)
                    .licensePlate("Chưa đăng ký biển số")
                    .vehicleType("MOTORBIKE")
                    .workingStatus("OFFLINE")
                    .kpiScore(5.0)
                    .completedOrders(0)
                    .build();
        }

        List<HandoverOrderDto> activeOrders = (driver.getId() != null)
                ? orderHandoverService.getDriverOrders(driver.getId(), List.of("OUT_FOR_DELIVERY", "DELIVERING", "ARRIVED"))
                : Collections.emptyList();

        List<com.ute.trasua.dto.driver.DriverTripHistoryDto> completedTrips = (driver.getId() != null)
                ? driverReportService.getDriverCompletedTrips(driver.getId())
                : Collections.emptyList();

        int todayCompletedCount = (int) completedTrips.stream()
                .filter(t -> "TODAY".equalsIgnoreCase(t.getPeriodCategory()))
                .count();
        int yesterdayCompletedCount = (int) completedTrips.stream()
                .filter(t -> "YESTERDAY".equalsIgnoreCase(t.getPeriodCategory()))
                .count();
        int weekCompletedCount = completedTrips.size();

        int completedCount = todayCompletedCount;

        boolean isOnline = "ONLINE".equalsIgnoreCase(driver.getWorkingStatus()) || "DELIVERING".equalsIgnoreCase(driver.getWorkingStatus());

        model.addAttribute("driver", driver);
        model.addAttribute("activeOrders", activeOrders);
        model.addAttribute("completedOrders", completedTrips);
        model.addAttribute("completedTrips", completedTrips);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("todayCompletedCount", todayCompletedCount);
        model.addAttribute("yesterdayCompletedCount", yesterdayCompletedCount);
        model.addAttribute("weekCompletedCount", weekCompletedCount);

        LocalDate today = LocalDate.now();
        model.addAttribute("todayDateIso", today.format(DateTimeFormatter.ISO_LOCAL_DATE));
        model.addAttribute("yesterdayDateIso", today.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        model.addAttribute("minDateMonthAgo", today.minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE));

        String username = (driverUser != null) ? driverUser.getUsername() : (authentication != null ? authentication.getName() : "driver");
        model.addAttribute("username", username);
        model.addAttribute("fullName", (driverUser != null && driverUser.getFullName() != null && !driverUser.getFullName().isBlank())
                ? driverUser.getFullName() : username);
        model.addAttribute("phone", (driverUser != null && driverUser.getPhone() != null && !driverUser.getPhone().isBlank())
                ? driverUser.getPhone() : "Chưa cập nhật SĐT");
        model.addAttribute("email", (driverUser != null && driverUser.getEmail() != null && !driverUser.getEmail().isBlank())
                ? driverUser.getEmail() : (username + "@utetra.vn"));
        model.addAttribute("driverCode", (driver.getId() != null) ? ("TX-" + String.format("%03d", driver.getId())) : "TX-CHUA-DANG-KY");
        model.addAttribute("vehiclePlate", (driver.getLicensePlate() != null && !driver.getLicensePlate().isBlank()) ? driver.getLicensePlate() : "Chưa đăng ký biển số");
        model.addAttribute("branchName", (driver.getBranch() != null && driver.getBranch().getName() != null)
                ? driver.getBranch().getName() : "Chưa phân công chi nhánh");
        model.addAttribute("isOnline", isOnline);

        return "shipper/index";
    }

    /**
     * API trả về danh sách đơn hàng đang giao của tài xế.
     */
    @GetMapping("/api/active-orders")
    @ResponseBody
    public ResponseEntity<List<HandoverOrderDto>> getActiveOrders(
            Authentication authentication) {
        Driver driver = resolveCurrentDriver(authentication, null);
        if (!isOwnDriver(authentication, driver)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
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
            Authentication authentication) {
        Driver driver = resolveCurrentDriver(authentication, null);
        if (!isOwnDriver(authentication, driver)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
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
            Authentication authentication) {
        Driver driver = resolveCurrentDriver(authentication, null);
        if (!isOwnDriver(authentication, driver)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
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
            Authentication authentication) {
        Driver driver = resolveCurrentDriver(authentication, null);
        if (!isOwnDriver(authentication, driver)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
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
            Authentication authentication) {
        Driver driver = resolveCurrentDriver(authentication, null);
        if (!isOwnDriver(authentication, driver)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
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
            Authentication authentication) {
        Driver driver = resolveCurrentDriver(authentication, null);
        if (!isOwnDriver(authentication, driver)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
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
    public ResponseEntity<Map<String, Object>> seedSampleOrder(
            Authentication authentication) {
        Driver driver = resolveCurrentDriver(authentication, null);
        if (!isOwnDriver(authentication, driver)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        String username = (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : "driver1";
        Order savedOrder = driverService.seedSampleDeliveryOrder(driver, username);

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
        if (!isOwnDriver(authentication, driver)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        if (driver != null) {
            try {
                driverService.toggleDriverAvailability(driver.getId(), online);
            } catch (IllegalStateException | org.springframework.dao.DataAccessException e) {
                // Ví dụ: còn ly trà đã lấy trên tay -> không cho tắt ca ngang
                return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                        .body(Map.of(
                                "success", false,
                                "online", !online,
                                "message", e.getMessage() != null ? e.getMessage() : "Không đổi được ca trực, thử lại sau!"));
            }
        }
        log.info("Shipper cập nhật trạng thái hoạt động: {}", online ? "BẬT (Online)" : "TẮT (Offline)");
        return ResponseEntity.ok(Map.of(
                "success", true,
                "online", online,
                "message", online ? "Đã bật ca trực, sẵn sàng tiếp nhận phân công đơn" : "Đã tắt ca trực, tạm ngưng nhận đơn"
        ));
    }

    /**
     * API trả về trạng thái trực ca THẬT trong DB của tài xế đang đăng nhập.
     * Dùng để giao diện tự đồng bộ lại công tắc (tránh cảnh tab hiển thị "TẮT" nhưng server vẫn giao đơn).
     */
    @GetMapping("/api/shift-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> shiftStatus(Authentication authentication) {
        Driver driver = resolveCurrentDriver(authentication, null);
        if (!isOwnDriver(authentication, driver)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        String status = (driver != null && driver.getWorkingStatus() != null)
                ? driver.getWorkingStatus().toUpperCase() : "OFFLINE";
        boolean online = "ONLINE".equals(status) || "DELIVERING".equals(status);
        return ResponseEntity.ok(Map.of(
                "online", online,
                "workingStatus", status));
    }

    private Driver resolveCurrentDriver(Authentication authentication, Long requestedDriverId) {
        return driverService.resolveCurrentDriver(authentication, requestedDriverId);
    }

    private boolean isOwnDriver(Authentication authentication, Driver driver) {
        if (driver == null || authentication == null) {
            return false;
        }
        if (authentication.getAuthorities() != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority())
                        || "ADMIN".equalsIgnoreCase(a.getAuthority())
                        || "ROLE_MANAGER".equalsIgnoreCase(a.getAuthority())
                        || "ROLE_BRANCH_MANAGER".equalsIgnoreCase(a.getAuthority()))) {
            return true;
        }
        return driver.getUser() != null
                && authentication.getName() != null
                && authentication.getName().equalsIgnoreCase(driver.getUser().getUsername());
    }
}
