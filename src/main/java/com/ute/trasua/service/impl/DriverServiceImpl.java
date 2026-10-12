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

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBER = "0123456789";
    private static final String SPECIAL = "@#$%&!";
    private static final String PASSWORD_ALLOW = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponse> searchDriversByBranch(Long branchId, String keyword, String shift, String workingStatus, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String cleanShift = (shift != null && !shift.isBlank()) ? shift.trim() : null;
        String cleanStatus = (workingStatus != null && !workingStatus.isBlank()) ? workingStatus.trim() : null;

        return driverRepository.searchDriversByBranch(branchId, cleanKeyword, cleanShift, cleanStatus, pageable)
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
                .vehicleType(request.getVehicleType() != null ? request.getVehicleType().trim() : "XE_MAY")
                .shift(request.getShift() != null ? request.getShift().trim() : "SANG")
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
        driver.setVehicleType(request.getVehicleType() != null ? request.getVehicleType().trim() : "XE_MAY");
        driver.setShift(request.getShift() != null ? request.getShift().trim() : "SANG");
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
}
