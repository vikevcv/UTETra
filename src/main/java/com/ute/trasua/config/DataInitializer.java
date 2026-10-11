package com.ute.trasua.config;

import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Khởi tạo dữ liệu mẫu cho hệ thống nếu chưa có dữ liệu.
 * Đảm bảo luôn có tài khoản Chủ chuỗi (ADMIN), Quản lý chi nhánh (MANAGER), Tài xế (DRIVER) và Khách hàng (CUSTOMER).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .email("admin@utetra.vn")
                    .fullName("Trần Thiên Bảo (Chủ Chuỗi)")
                    .phone("0901234567")
                    .enabled(true)
                    .locked(false)
                    .roles(Set.of("ROLE_ADMIN", "ROLE_MANAGER"))
                    .createdAt(LocalDateTime.now().minusDays(30))
                    .build();
            userRepository.save(admin);
            log.info(">>> Đã khởi tạo tài khoản ADMIN: admin / Admin@123");
        }

        if (!userRepository.existsByUsername("manager1")) {
            User manager = User.builder()
                    .username("manager1")
                    .password(passwordEncoder.encode("Manager@123"))
                    .email("manager1@utetra.vn")
                    .fullName("Nguyễn Văn Quản Lý (CN Thủ Đức)")
                    .phone("0912345678")
                    .enabled(true)
                    .locked(false)
                    .roles(Set.of("ROLE_MANAGER"))
                    .createdAt(LocalDateTime.now().minusDays(20))
                    .build();
            userRepository.save(manager);
            log.info(">>> Đã khởi tạo tài khoản MANAGER: manager1 / Manager@123");
        }

        if (!userRepository.existsByUsername("driver1")) {
            User driver = User.builder()
                    .username("driver1")
                    .password(passwordEncoder.encode("Driver@123"))
                    .email("driver1@utetra.vn")
                    .fullName("Phạm Văn Giao Hàng")
                    .phone("0923456789")
                    .enabled(true)
                    .locked(false)
                    .roles(Set.of("ROLE_DRIVER"))
                    .createdAt(LocalDateTime.now().minusDays(15))
                    .build();
            userRepository.save(driver);
            log.info(">>> Đã khởi tạo tài khoản DRIVER: driver1 / Driver@123");
        }

        if (!userRepository.existsByUsername("driver2")) {
            User driver2 = User.builder()
                    .username("driver2")
                    .password(passwordEncoder.encode("Driver@123"))
                    .email("driver2@utetra.vn")
                    .fullName("Trần Quốc Tốc Độ")
                    .phone("0988776655")
                    .enabled(true)
                    .locked(false)
                    .roles(Set.of("ROLE_DRIVER"))
                    .createdAt(LocalDateTime.now().minusDays(8))
                    .build();
            userRepository.save(driver2);
        }

        if (!userRepository.existsByUsername("driver3")) {
            User driver3 = User.builder()
                    .username("driver3")
                    .password(passwordEncoder.encode("Driver@123"))
                    .email("driver3@utetra.vn")
                    .fullName("Vũ Hoàng Shipper (Tạm Khóa)")
                    .phone("0977665544")
                    .enabled(true)
                    .locked(true)
                    .lockReason("Tài xế giao trễ 4 đơn liên tục không báo trước")
                    .roles(Set.of("ROLE_DRIVER"))
                    .createdAt(LocalDateTime.now().minusDays(12))
                    .build();
            userRepository.save(driver3);
        }

        if (!userRepository.existsByUsername("customer1")) {
            User customer1 = User.builder()
                    .username("customer1")
                    .password(passwordEncoder.encode("Customer@123"))
                    .email("customer1@gmail.com")
                    .fullName("Lê Thị Trà Sữa")
                    .phone("0934567890")
                    .enabled(true)
                    .locked(false)
                    .roles(Set.of("ROLE_CUSTOMER"))
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .build();
            userRepository.save(customer1);
        }

        if (!userRepository.existsByUsername("spammer")) {
            User lockedUser = User.builder()
                    .username("spammer")
                    .password(passwordEncoder.encode("Customer@123"))
                    .email("spammer@gmail.com")
                    .fullName("Nguyễn Văn Boom Hàng")
                    .phone("0945678901")
                    .enabled(true)
                    .locked(true)
                    .lockReason("Vi phạm chính sách: Đặt đơn ảo và boom hàng 3 lần liên tiếp")
                    .roles(Set.of("ROLE_CUSTOMER"))
                    .createdAt(LocalDateTime.now().minusDays(5))
                    .build();
            userRepository.save(lockedUser);
        }
    }
}
