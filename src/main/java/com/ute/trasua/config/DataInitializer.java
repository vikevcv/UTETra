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
    private final com.ute.trasua.repository.BranchRepository branchRepository;
    private final com.ute.trasua.repository.EmployeeRepository employeeRepository;
    private final com.ute.trasua.repository.PosAccountRepository posAccountRepository;
    private final com.ute.trasua.repository.KichCoRepository kichCoRepository;
    private final com.ute.trasua.repository.ToppingRepository toppingRepository;
    private final com.ute.trasua.repository.DriverRepository driverRepository;
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

        // Khởi tạo Chi Nhánh mẫu (UC21)
        if (!branchRepository.existsByBranchCode("CN-TD01")) {
            User manager1 = userRepository.findByUsername("manager1").orElse(null);
            com.ute.trasua.entity.Branch cnThuDuc = com.ute.trasua.entity.Branch.builder()
                    .branchCode("CN-TD01")
                    .name("UTETea Cơ Sở Thủ Đức (Đại Học SPKT)")
                    .address("Số 1 Võ Văn Ngân, P. Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh")
                    .latitude(10.85072)
                    .longitude(106.77196)
                    .hotline("028.3896.8641")
                    .openTime("07:00")
                    .closeTime("22:30")
                    .active(true)
                    .manager(manager1)
                    .createdAt(LocalDateTime.now().minusDays(30))
                    .build();
            cnThuDuc = branchRepository.save(cnThuDuc);
            log.info(">>> Đã khởi tạo Chi Nhánh CN-TD01: {}", cnThuDuc.getName());

            // Nhân sự mẫu cho CN-TD01 (UC16)
            if (!employeeRepository.existsByEmployeeCode("NV001")) {
                employeeRepository.save(com.ute.trasua.entity.Employee.builder()
                        .employeeCode("NV001")
                        .fullName("Nguyễn Thị Hồng")
                        .phone("0911223344")
                        .citizenId("079198001234")
                        .email("hong.nguyen@utetra.vn")
                        .position("BARTENDER")
                        .shift("MORNING")
                        .salary(7500000.0)
                        .status("ACTIVE")
                        .hireDate(java.time.LocalDate.now().minusMonths(6))
                        .branch(cnThuDuc)
                        .build());
            }

            if (!employeeRepository.existsByEmployeeCode("NV002")) {
                employeeRepository.save(com.ute.trasua.entity.Employee.builder()
                        .employeeCode("NV002")
                        .fullName("Trần Minh Nhật")
                        .phone("0922334455")
                        .citizenId("079200005678")
                        .email("nhat.tran@utetra.vn")
                        .position("CASHIER")
                        .shift("AFTERNOON")
                        .salary(6800000.0)
                        .status("ACTIVE")
                        .hireDate(java.time.LocalDate.now().minusMonths(4))
                        .branch(cnThuDuc)
                        .build());
            }

            if (!employeeRepository.existsByEmployeeCode("NV003")) {
                employeeRepository.save(com.ute.trasua.entity.Employee.builder()
                        .employeeCode("NV003")
                        .fullName("Lê Hoàng Nam")
                        .phone("0933445566")
                        .citizenId("079201009988")
                        .email("nam.le@utetra.vn")
                        .position("SERVER")
                        .shift("EVENING")
                        .salary(5500000.0)
                        .status("ACTIVE")
                        .hireDate(java.time.LocalDate.now().minusMonths(2))
                        .branch(cnThuDuc)
                        .build());
            }

            if (!employeeRepository.existsByEmployeeCode("NV004")) {
                employeeRepository.save(com.ute.trasua.entity.Employee.builder()
                        .employeeCode("NV004")
                        .fullName("Võ Thị Bích Thảo")
                        .phone("0944556677")
                        .citizenId("079199003322")
                        .email("thao.vo@utetra.vn")
                        .position("BARTENDER")
                        .shift("FULL_TIME")
                        .salary(8500000.0)
                        .status("ACTIVE")
                        .hireDate(java.time.LocalDate.now().minusMonths(8))
                        .branch(cnThuDuc)
                        .build());
            }

            // Tài khoản quầy POS (UC16)
            if (!posAccountRepository.existsByUsername("pos_td01")) {
                posAccountRepository.save(com.ute.trasua.entity.PosAccount.builder()
                        .counterName("Quầy Thu Ngân 01 - Thủ Đức")
                        .username("pos_td01")
                        .password(passwordEncoder.encode("Pos@123"))
                        .active(true)
                        .note("Máy POS quầy mặt tiền sảnh chính")
                        .branch(cnThuDuc)
                        .build());
            }
        }

        if (!branchRepository.existsByBranchCode("CN-Q01")) {
            User admin = userRepository.findByUsername("admin").orElse(null);
            com.ute.trasua.entity.Branch cnQuan1 = com.ute.trasua.entity.Branch.builder()
                    .branchCode("CN-Q01")
                    .name("UTETea Flagship Quận 1 (Phố Đi Bộ)")
                    .address("68 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh")
                    .latitude(10.77431)
                    .longitude(106.70322)
                    .hotline("028.3822.9999")
                    .openTime("07:30")
                    .closeTime("23:00")
                    .active(true)
                    .manager(admin)
                    .createdAt(LocalDateTime.now().minusDays(25))
                    .build();
            cnQuan1 = branchRepository.save(cnQuan1);
            log.info(">>> Đã khởi tạo Chi Nhánh CN-Q01: {}", cnQuan1.getName());

            if (!posAccountRepository.existsByUsername("pos_q101")) {
                posAccountRepository.save(com.ute.trasua.entity.PosAccount.builder()
                        .counterName("Quầy Thu Ngân 01 - Quận 1")
                        .username("pos_q101")
                        .password(passwordEncoder.encode("Pos@123"))
                        .active(true)
                        .note("Máy POS quầy sảnh tầng trệt")
                        .branch(cnQuan1)
                        .build());
            }
        }

        // ==========================================
        // UC23: Khởi tạo Kích cỡ ly (Bảng KichCo) và Topping (Bảng Topping) theo ERD Báo Cáo SE
        // ==========================================
        if (kichCoRepository.count() == 0) {
            java.util.List<com.ute.trasua.entity.KichCo> initialSizes = java.util.List.of(
                    com.ute.trasua.entity.KichCo.builder()
                            .maKichCo("S")
                            .tenKichCo("Size S - Cỡ Nhỏ (350ml)")
                            .giaPhuThu(java.math.BigDecimal.ZERO)
                            .trangThai("HoatDong")
                            .build(),
                    com.ute.trasua.entity.KichCo.builder()
                            .maKichCo("M")
                            .tenKichCo("Size M - Cỡ Tiêu Chuẩn (500ml)")
                            .giaPhuThu(java.math.BigDecimal.ZERO)
                            .trangThai("HoatDong")
                            .build(),
                    com.ute.trasua.entity.KichCo.builder()
                            .maKichCo("L")
                            .tenKichCo("Size L - Cỡ Lớn (700ml)")
                            .giaPhuThu(new java.math.BigDecimal("6000"))
                            .trangThai("HoatDong")
                            .build()
            );
            kichCoRepository.saveAll(initialSizes);
            log.info(">>> Đã khởi tạo {} kích cỡ mặc định (Bảng KichCo)", initialSizes.size());
        }

        if (toppingRepository.count() == 0) {
            java.util.List<com.ute.trasua.entity.Topping> initialToppings = java.util.List.of(
                    com.ute.trasua.entity.Topping.builder()
                            .tenTopping("Trân Châu Đen Dẻo")
                            .donGia(new java.math.BigDecimal("8000"))
                            .trangThai("HoatDong")
                            .build(),
                    com.ute.trasua.entity.Topping.builder()
                            .tenTopping("Trân Châu Hoàng Kim")
                            .donGia(new java.math.BigDecimal("10000"))
                            .trangThai("HoatDong")
                            .build(),
                    com.ute.trasua.entity.Topping.builder()
                            .tenTopping("Thạch Củ Năng Giòn")
                            .donGia(new java.math.BigDecimal("10000"))
                            .trangThai("HoatDong")
                            .build(),
                    com.ute.trasua.entity.Topping.builder()
                            .tenTopping("Kem Phô Mai Cheese Foam")
                            .donGia(new java.math.BigDecimal("12000"))
                            .trangThai("HoatDong")
                            .build(),
                    com.ute.trasua.entity.Topping.builder()
                            .tenTopping("Thạch Matcha Nhật Bản")
                            .donGia(new java.math.BigDecimal("9000"))
                            .trangThai("HoatDong")
                            .build()
            );
            toppingRepository.saveAll(initialToppings);
            log.info(">>> Đã khởi tạo {} loại topping mặc định (Bảng Topping)", initialToppings.size());
        }

        // ==========================================
        // UC17: Khởi tạo Hồ Sơ Tài Xế Giao Hàng Chi Nhánh (Bảng TaiXe)
        // ==========================================
        if (driverRepository.count() == 0) {
            com.ute.trasua.entity.Branch defaultBranch = branchRepository.findAll().stream().findFirst().orElse(null);
            if (defaultBranch != null) {
                User d1 = userRepository.findByUsername("driver1").orElse(null);
                User d2 = userRepository.findByUsername("driver2").orElse(null);
                User d3 = userRepository.findByUsername("driver3").orElse(null);

                if (d1 != null) {
                    driverRepository.save(com.ute.trasua.entity.Driver.builder()
                            .user(d1)
                            .branch(defaultBranch)
                            .licensePlate("59-X1 888.88")
                            .vehicleType("XE_MAY")
                            .shift("SANG")
                            .workingStatus("ONLINE")
                            .baseSalary(new java.math.BigDecimal("6000000"))
                            .kpiScore(4.9)
                            .completedOrders(185)
                            .build());
                }

                if (d2 != null) {
                    driverRepository.save(com.ute.trasua.entity.Driver.builder()
                            .user(d2)
                            .branch(defaultBranch)
                            .licensePlate("59-S2 345.67")
                            .vehicleType("XE_MAY")
                            .shift("CHIEU")
                            .workingStatus("DELIVERING")
                            .baseSalary(new java.math.BigDecimal("6000000"))
                            .kpiScore(4.8)
                            .completedOrders(210)
                            .build());
                }

                if (d3 != null) {
                    driverRepository.save(com.ute.trasua.entity.Driver.builder()
                            .user(d3)
                            .branch(defaultBranch)
                            .licensePlate("50-T1 999.12")
                            .vehicleType("XE_DIEN")
                            .shift("TOAN_THOI_GIAN")
                            .workingStatus("OFFLINE")
                            .baseSalary(new java.math.BigDecimal("5500000"))
                            .kpiScore(4.2)
                            .completedOrders(74)
                            .build());
                }
                log.info(">>> Đã khởi tạo hồ sơ tài xế mẫu trực thuộc chi nhánh [{}]", defaultBranch.getName());
            }
        }
    }
}
