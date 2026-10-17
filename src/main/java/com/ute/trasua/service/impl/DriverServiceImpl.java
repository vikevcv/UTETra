package com.ute.trasua.service.impl;

import com.ute.trasua.dto.CreateDriverRequest;
import com.ute.trasua.dto.DriverResponse;
import com.ute.trasua.dto.UpdateDriverRequest;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Driver;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.DriverRepository;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.DriverService;
import com.ute.trasua.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final com.ute.trasua.repository.OrderRepository orderRepository;
    private final com.ute.trasua.service.DispatchNotifier dispatchNotifier;

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBER = "0123456789";
    private static final String SPECIAL = "@#$%&!";
    private static final String PASSWORD_ALLOW = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponse> searchDriversByBranch(Long branchId, String keyword, String workingStatus, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String cleanStatus = (workingStatus != null && !workingStatus.isBlank()) ? workingStatus.trim() : null;

        return driverRepository.searchDriversByBranch(branchId, cleanKeyword, cleanStatus, pageable)
                .map(DriverResponse::fromEntity);
    }

    @Override
    @Transactional
    public DriverResponse createDriver(CreateDriverRequest request, Long branchId) {
        Long targetBranchId = request.getBranchId() != null ? request.getBranchId() : branchId;
        if (targetBranchId == null) {
            throw new IllegalArgumentException("Không xác định được chi nhánh tiếp nhận tài xế");
        }

        Branch branch = branchRepository.findById(targetBranchId)
                .orElseThrow(() -> new IllegalArgumentException("Chi nhánh không tồn tại với ID: " + targetBranchId));

        String cleanEmail = request.getEmail().trim().toLowerCase();
        String cleanPhone = request.getPhone().trim();
        String cleanPlate = request.getLicensePlate().trim().toUpperCase();

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Email '" + cleanEmail + "' đã được sử dụng trong hệ thống.");
        }

        if (driverRepository.existsByLicensePlate(cleanPlate)) {
            throw new IllegalArgumentException("Biển số xe '" + cleanPlate + "' đã được đăng ký cho tài xế khác.");
        }

        // Tạo username duy nhất từ SĐT
        String generatedUsername = "tx_" + cleanPhone;
        int suffix = 1;
        while (userRepository.existsByUsername(generatedUsername)) {
            generatedUsername = "tx_" + cleanPhone + "_" + suffix++;
        }

        // Tự động sinh mật khẩu ngẫu nhiên an toàn (8 ký tự)
        String rawPassword = generateRandomPassword(8);

        // 1. Lưu User với vai trò ROLE_DRIVER
        User user = User.builder()
                .username(generatedUsername)
                .password(passwordEncoder.encode(rawPassword))
                .email(cleanEmail)
                .fullName(request.getFullName().trim())
                .phone(cleanPhone)
                .enabled(true)
                .locked(false)
                .roles(Set.of("ROLE_DRIVER"))
                .createdAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        // 2. Lưu Driver liên kết Chi nhánh
        Driver driver = Driver.builder()
                .user(user)
                .branch(branch)
                .licensePlate(cleanPlate)
                .vehicleType(request.getVehicleType() != null ? request.getVehicleType().trim() : "MOTORBIKE")
                .workingStatus("ONLINE")
                .baseSalary(request.getBaseSalary() != null ? request.getBaseSalary() : BigDecimal.ZERO)
                .kpiScore(5.0)
                .completedOrders(0)
                .build();
        driver = driverRepository.save(driver);

        log.info(">>> Đã tạo tài khoản tài xế thành công! Username: [{}], RawPassword: [{}], Chi nhánh: [{}]",
                generatedUsername, rawPassword, branch.getName());

        // 3. Gửi Email thông báo tài khoản & mật khẩu qua Brevo SMTP ngầm
        try {
            emailService.sendDriverWelcomeEmail(
                    cleanEmail,
                    user.getFullName(),
                    generatedUsername,
                    rawPassword,
                    branch.getName()
            );
        } catch (Exception e) {
            log.warn("Không thể gửi email kích hoạt đến [{}]: {}", cleanEmail, e.getMessage());
        }

        return DriverResponse.fromEntity(driver);
    }

    @Override
    @Transactional
    public DriverResponse updateDriver(Long driverId, UpdateDriverRequest request) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài xế với ID: " + driverId));

        String cleanPlate = request.getLicensePlate().trim().toUpperCase();
        if (driverRepository.existsByLicensePlateAndIdNot(cleanPlate, driverId)) {
            throw new IllegalArgumentException("Biển số xe '" + cleanPlate + "' đã được đăng ký cho tài xế khác.");
        }

        User user = driver.getUser();
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        userRepository.save(user);

        driver.setLicensePlate(cleanPlate);
        driver.setVehicleType(request.getVehicleType() != null ? request.getVehicleType().trim() : "MOTORBIKE");
        driver.setBaseSalary(request.getBaseSalary() != null ? request.getBaseSalary() : BigDecimal.ZERO);
        driver = driverRepository.save(driver);

        log.info(">>> Đã cập nhật tài xế [ID: {} - {}], lương mới: {}", driverId, user.getFullName(), driver.getBaseSalary());
        return DriverResponse.fromEntity(driver);
    }

    @Override
    @Transactional
    public DriverResponse toggleDriverLock(Long driverId, boolean locked, String reason) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài xế với ID: " + driverId));

        User user = driver.getUser();
        user.setLocked(locked);
        user.setLockReason(locked ? (reason != null ? reason : "Tạm ngưng tiếp nhận đơn") : null);
        userRepository.save(user);

        driver.setWorkingStatus(locked ? "OFFLINE" : "ONLINE");
        driver = driverRepository.save(driver);

        return DriverResponse.fromEntity(driver);
    }

    @Override
    @Transactional
    public DriverResponse updateWorkingStatus(Long driverId, String newStatus) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài xế với ID: " + driverId));

        driver.setWorkingStatus(newStatus);
        driver = driverRepository.save(driver);
        return DriverResponse.fromEntity(driver);
    }

    @Override
    @Transactional
    public void resendDriverCredentials(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài xế với ID: " + driverId));

        User user = driver.getUser();
        String newPassword = generateRandomPassword(8);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info(">>> Đã reset mật khẩu tài xế [{}]: Mật khẩu mới: [{}]", user.getUsername(), newPassword);

        try {
            emailService.sendDriverWelcomeEmail(
                    user.getEmail(),
                    user.getFullName(),
                    user.getUsername(),
                    newPassword,
                    driver.getBranch().getName()
            );
        } catch (Exception e) {
            log.error("Lỗi gửi lại email mật khẩu tài xế: {}", e.getMessage());
            throw new RuntimeException("Lỗi gửi email: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBranchDriverStatistics(Long branchId) {
        long total = driverRepository.countByBranchId(branchId);
        long online = driverRepository.countByBranchIdAndWorkingStatus(branchId, "ONLINE");
        long delivering = driverRepository.countByBranchIdAndWorkingStatus(branchId, "DELIVERING");
        long suspended = driverRepository.countSuspendedByBranchId(branchId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("online", online);
        stats.put("delivering", delivering);
        stats.put("suspended", suspended);
        return stats;
    }

    private String generateRandomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        sb.append(CHAR_LOWER.charAt(RANDOM.nextInt(CHAR_LOWER.length())));
        sb.append(CHAR_UPPER.charAt(RANDOM.nextInt(CHAR_UPPER.length())));
        sb.append(NUMBER.charAt(RANDOM.nextInt(NUMBER.length())));
        sb.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));

        for (int i = 4; i < length; i++) {
            sb.append(PASSWORD_ALLOW.charAt(RANDOM.nextInt(PASSWORD_ALLOW.length())));
        }

        // Xáo trộn chuỗi
        char[] array = sb.toString().toCharArray();
        for (int i = array.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
        return new String(array);
    }

    @Override
    @Transactional
    public DriverResponse toggleDriverAvailability(Long driverId, Boolean isAvailable) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài xế ID: " + driverId));

        boolean available = Boolean.TRUE.equals(isAvailable);
        driver.setWorkingStatus(available ? "ONLINE" : "OFFLINE");
        Driver saved = driverRepository.save(driver);

        if (!available) {
            long stillActive = orderRepository.countByDriverIdAndStatusIn(driverId, List.of("OUT_FOR_DELIVERY", "DELIVERING", "ARRIVED"));
            if (stillActive > 0) {
                log.warn(">>> CẢNH BÁO: Tài xế {} tắt ca trực nhưng vẫn còn {} đơn đang giao!", driverId, stillActive);
            }
        }

        log.info(">>> Tài xế {} đã chuyển trạng thái trực ca: {}", driverId, saved.getWorkingStatus());
        return DriverResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public com.ute.trasua.dto.handover.HandoverOrderDto reportForceMajeureIncident(Long orderId, Long driverId, String incidentReason, String note) {
        String cleanReason = (incidentReason != null) ? incidentReason.trim().toUpperCase() : "";
        Set<String> validReasons = Set.of("XE_HONG", "SUC_KHOE", "KET_DON", "KHACH_HUY");
        if (!validReasons.contains(cleanReason)) {
            throw new IllegalArgumentException("Mã lý do sự cố không hợp lệ: " + incidentReason);
        }

        com.ute.trasua.entity.Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        Driver assignedDriver = order.getDriver();
        if (assignedDriver == null) {
            throw new IllegalStateException("Đơn hàng " + order.getOrderCode() + " đã được giải phóng hoặc chưa được gán tài xế!");
        }

        if (!assignedDriver.getId().equals(driverId)) {
            throw new IllegalStateException("Quyền truy cập bị từ chối: Bạn không phải tài xế được phân công phụ trách đơn hàng này!");
        }

        String currentStatus = (order.getStatus() != null) ? order.getStatus().toUpperCase() : "";
        if (!Set.of("OUT_FOR_DELIVERY", "DELIVERING", "ARRIVED").contains(currentStatus)) {
            throw new IllegalStateException("Đơn hàng " + order.getOrderCode() + " không ở trạng thái đang giao vận (Hiện tại: " + currentStatus + ") nên không thể báo sự cố!");
        }

        boolean isCustomerCancelled = "KHACH_HUY".equals(cleanReason);

        order.setIncidentReason(cleanReason);
        order.setIncidentNote(note != null && !note.isBlank() ? note.trim() : null);
        order.setIncidentReportedAt(LocalDateTime.now());
        order.setStatus(isCustomerCancelled ? "CANCELLED" : "READY_FOR_DELIVERY");
        order.setDriver(null);

        com.ute.trasua.entity.Order savedOrder = orderRepository.save(order);

        // Cập nhật trạng thái tài xế
        boolean mustOffline = "XE_HONG".equals(cleanReason) || "SUC_KHOE".equals(cleanReason);
        if (mustOffline) {
            assignedDriver.setWorkingStatus("OFFLINE");
        } else {
            long remaining = orderRepository.countByDriverIdAndStatusIn(assignedDriver.getId(), List.of("OUT_FOR_DELIVERY", "DELIVERING", "ARRIVED"));
            assignedDriver.setWorkingStatus(remaining > 0 ? "DELIVERING" : "ONLINE");
        }
        driverRepository.save(assignedDriver);

        // Bắn WebSocket thông báo tới Quản lý chi nhánh, Tài xế và Khách hàng
        dispatchNotifier.notifyDriverIncident(savedOrder, assignedDriver, cleanReason, note);
        if (isCustomerCancelled) {
            dispatchNotifier.notifyOrderCancelled(savedOrder, note);
        }

        log.warn(">>> [FORCE MAJEURE] Tài xế {} báo sự cố {} cho đơn {}. Đơn chuyển sang: {}",
                assignedDriver.getId(), cleanReason, savedOrder.getOrderCode(), savedOrder.getStatus());

        return mapToHandoverDto(savedOrder);
    }

    private com.ute.trasua.dto.handover.HandoverOrderDto mapToHandoverDto(com.ute.trasua.entity.Order order) {
        int totalCups = 0;
        java.util.List<String> items = new java.util.ArrayList<>();
        if (order.getOrderDetails() != null) {
            for (com.ute.trasua.entity.OrderDetail d : order.getOrderDetails()) {
                int q = (d.getQuantity() != null && d.getQuantity() > 0) ? d.getQuantity() : 1;
                totalCups += q;
                items.add(d.getProductName() + " x" + q);
            }
        }

        return com.ute.trasua.dto.handover.HandoverOrderDto.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .dailyQueueNumber(order.getDailyQueueNumber() != null ? order.getDailyQueueNumber() : 1)
                .orderType(order.getOrderType() != null ? order.getOrderType() : "DELIVERY")
                .status(order.getStatus())
                .customerName(order.getCustomerName() != null ? order.getCustomerName() : "Khách hàng")
                .customerPhone(order.getCustomerPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .cupCount(totalCups > 0 ? totalCups : 1)
                .itemsSummary(String.join(", ", items))
                .createdAt(order.getCreatedAt())
                .incidentReason(order.getIncidentReason())
                .incidentNote(order.getIncidentNote())
                .incidentReportedAt(order.getIncidentReportedAt())
                .build();
    }
}
