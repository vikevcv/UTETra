package com.ute.trasua.config;

import com.ute.trasua.entity.*;
import com.ute.trasua.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final com.ute.trasua.repository.CategoryRepository categoryRepository;
    private final com.ute.trasua.repository.ProductRepository productRepository;
    private final com.ute.trasua.repository.SizeRepository sizeRepository;
    private final com.ute.trasua.repository.ProductSizeRepository productSizeRepository;
    private final com.ute.trasua.repository.ToppingRepository toppingRepository;
    private final com.ute.trasua.repository.DriverRepository driverRepository;
    private final com.ute.trasua.repository.IngredientRepository ingredientRepository;
    private final com.ute.trasua.repository.BranchInventoryRepository branchInventoryRepository;
    private final com.ute.trasua.repository.ImportReceiptRepository importReceiptRepository;
    private final com.ute.trasua.repository.OrderRepository orderRepository;
    private final com.ute.trasua.repository.PromotionRepository promotionRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        // Cập nhật schema nếu cột size_name trong order_details bị giới hạn 20 ký tự
        try {
            jdbcTemplate.execute("ALTER TABLE order_details MODIFY size_name VARCHAR(100)");
            jdbcTemplate.execute("ALTER TABLE order_details MODIFY sugar_level VARCHAR(50)");
            jdbcTemplate.execute("ALTER TABLE order_details MODIFY ice_level VARCHAR(50)");
            log.info(">>> Đã cập nhật độ dài cột bảng order_details (size_name=100, sugar_level=50, ice_level=50)");
        } catch (Exception e) {
            log.debug("Notice on altering order_details columns: {}", e.getMessage());
        }

        // Cập nhật hoặc tạo tài khoản POS: tên chi nhánh viết liền (thuduc, quan1) - mật khẩu 123123
        branchRepository.findAll().forEach(b -> {
            String slug = java.text.Normalizer.normalize(b.getName(), java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                    .toLowerCase();
            String username = "thuduc";
            if (slug.contains("quan 1") || b.getBranchCode().contains("Q01")) {
                username = "quan1";
            }
            final String finalUser = username;
            String shortName = "Quầy POS " + (finalUser.equals("thuduc") ? "Thủ Đức" : "Quận 1");
            PosAccount pos = posAccountRepository.findByUsername(finalUser).orElse(null);
            if (pos == null) {
                pos = PosAccount.builder()
                        .counterName(shortName)
                        .username(finalUser)
                        .password(passwordEncoder.encode("123123"))
                        .active(true)
                        .branch(b)
                        .note("Tài khoản máy POS quầy theo chi nhánh")
                        .build();
            } else {
                pos.setCounterName(shortName);
                pos.setPassword(passwordEncoder.encode("123123"));
                pos.setActive(true);
                pos.setBranch(b);
            }
            posAccountRepository.save(pos);
            log.info(">>> Đã cập nhật tài khoản POS chi nhánh {}: username={} / password=123123", b.getName(), finalUser);
        });

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
                        .salary(8500000.0)
                        .status("ACTIVE")
                        .hireDate(java.time.LocalDate.now().minusMonths(8))
                        .branch(cnThuDuc)
                        .build());
            }

            if (!employeeRepository.existsByEmployeeCode("NV005")) {
                employeeRepository.save(com.ute.trasua.entity.Employee.builder()
                        .employeeCode("NV005")
                        .fullName("Phạm Gia Hân")
                        .phone("0955667788")
                        .citizenId("079203001122")
                        .email("han.pham@utetra.vn")
                        .salary(6900000.0)
                        .status("ACTIVE")
                        .hireDate(java.time.LocalDate.now().minusMonths(3))
                        .branch(cnThuDuc)
                        .build());
            }

            if (!employeeRepository.existsByEmployeeCode("NV006")) {
                employeeRepository.save(com.ute.trasua.entity.Employee.builder()
                        .employeeCode("NV006")
                        .fullName("Đỗ Hoàng Long")
                        .phone("0966778899")
                        .citizenId("079202004455")
                        .email("long.do@utetra.vn")
                        .salary(8200000.0)
                        .status("ACTIVE")
                        .hireDate(java.time.LocalDate.now().minusMonths(5))
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
        // UC23: Initialize Product Size (table kich_co) and Topping (table topping) per ERD
        // ==========================================
        if (productSizeRepository.count() == 0) {
            java.util.List<com.ute.trasua.entity.ProductSize> initialSizes = java.util.List.of(
                    com.ute.trasua.entity.ProductSize.builder()
                            .sizeCode("S")
                            .sizeName("Size S - Cỡ Nhỏ (350ml)")
                            .additionalPrice(java.math.BigDecimal.ZERO)
                            .status("ACTIVE")
                            .build(),
                    com.ute.trasua.entity.ProductSize.builder()
                            .sizeCode("M")
                            .sizeName("Size M - Cỡ Tiêu Chuẩn (500ml)")
                            .additionalPrice(java.math.BigDecimal.ZERO)
                            .status("ACTIVE")
                            .build(),
                    com.ute.trasua.entity.ProductSize.builder()
                            .sizeCode("L")
                            .sizeName("Size L - Cỡ Lớn (700ml)")
                            .additionalPrice(new java.math.BigDecimal("6000"))
                            .status("ACTIVE")
                            .build()
            );
            productSizeRepository.saveAll(initialSizes);
            log.info(">>> Initialized {} default product sizes (table kich_co)", initialSizes.size());
        }

        if (toppingRepository.count() == 0) {
            java.util.List<com.ute.trasua.entity.Topping> initialToppings = java.util.List.of(
                    com.ute.trasua.entity.Topping.builder()
                            .toppingName("Trân Châu Đen Dẻo")
                            .price(new java.math.BigDecimal("8000"))
                            .status("ACTIVE")
                            .build(),
                    com.ute.trasua.entity.Topping.builder()
                            .toppingName("Trân Châu Hoàng Kim")
                            .price(new java.math.BigDecimal("10000"))
                            .status("ACTIVE")
                            .build(),
                    com.ute.trasua.entity.Topping.builder()
                            .toppingName("Thạch Củ Năng Giòn")
                            .price(new java.math.BigDecimal("10000"))
                            .status("ACTIVE")
                            .build(),
                    com.ute.trasua.entity.Topping.builder()
                            .toppingName("Kem Phô Mai Cheese Foam")
                            .price(new java.math.BigDecimal("12000"))
                            .status("ACTIVE")
                            .build(),
                    com.ute.trasua.entity.Topping.builder()
                            .toppingName("Thạch Matcha Nhật Bản")
                            .price(new java.math.BigDecimal("9000"))
                            .status("ACTIVE")
                            .build()
            );
            toppingRepository.saveAll(initialToppings);
            log.info(">>> Initialized {} default toppings (table topping)", initialToppings.size());
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
                            .vehicleType("MOTORBIKE")
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
                            .vehicleType("MOTORBIKE")
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
                            .vehicleType("ELECTRIC_BIKE")
                            .workingStatus("OFFLINE")
                            .baseSalary(new java.math.BigDecimal("5500000"))
                            .kpiScore(4.2)
                            .completedOrders(74)
                            .build());
                }
                log.info(">>> Đã khởi tạo hồ sơ tài xế mẫu trực thuộc chi nhánh [{}]", defaultBranch.getName());
            }
        }

        initMenuData();
    }

    private void initMenuData() {
        // 1. Khởi tạo Kích cỡ ly (Size)
        if (sizeRepository.count() == 0) {
            sizeRepository.save(com.ute.trasua.entity.Size.builder()
                    .name("M")
                    .volumeMl(500)
                    .extraPrice(java.math.BigDecimal.ZERO)
                    .active(true)
                    .build());
            sizeRepository.save(com.ute.trasua.entity.Size.builder()
                    .name("L")
                    .volumeMl(700)
                    .extraPrice(new java.math.BigDecimal("8000"))
                    .active(true)
                    .build());
            log.info(">>> Đã khởi tạo danh sách Kích Cỡ ly (M, L)");
        }

        // 2. Khởi tạo Topping
        if (toppingRepository.count() == 0) {
            toppingRepository.save(com.ute.trasua.entity.Topping.builder()
                    .name("Trân Châu Đen Dẻo")
                    .price(new java.math.BigDecimal("6000"))
                    .status("ACTIVE")
                    .build());
            toppingRepository.save(com.ute.trasua.entity.Topping.builder()
                    .name("Thạch Phô Mai Tươi")
                    .price(new java.math.BigDecimal("8000"))
                    .status("ACTIVE")
                    .build());
            toppingRepository.save(com.ute.trasua.entity.Topping.builder()
                    .name("Pudding Trứng Béo Ngậy")
                    .price(new java.math.BigDecimal("7000"))
                    .status("ACTIVE")
                    .build());
            toppingRepository.save(com.ute.trasua.entity.Topping.builder()
                    .name("Trân Châu Hoàng Kim")
                    .price(new java.math.BigDecimal("8000"))
                    .status("ACTIVE")
                    .build());
            log.info(">>> Đã khởi tạo 4 loại Topping quầy bán");
        }

        // 3. Khởi tạo Danh mục & Sản phẩm
        if (categoryRepository.count() == 0) {
            com.ute.trasua.entity.Category cat1 = categoryRepository.save(com.ute.trasua.entity.Category.builder()
                    .name("Trà Sữa Truyền Thống")
                    .description("Hương vị đậm đà nguyên bản từ lá trà tuyển chọn")
                    .displayOrder(1)
                    .active(true)
                    .build());

            com.ute.trasua.entity.Category cat2 = categoryRepository.save(com.ute.trasua.entity.Category.builder()
                    .name("Trà Trái Cây Thanh Nhiệt")
                    .description("Hòa quyện nước cốt trà thanh khiết cùng trái cây tươi mát")
                    .displayOrder(2)
                    .active(true)
                    .build());

            com.ute.trasua.entity.Category cat3 = categoryRepository.save(com.ute.trasua.entity.Category.builder()
                    .name("Macchiato Kem Cheese")
                    .description("Lớp bọt sữa cheese foam sánh mịn phủ trên cốt trà đậm đà")
                    .displayOrder(3)
                    .active(true)
                    .build());

            com.ute.trasua.entity.Category cat4 = categoryRepository.save(com.ute.trasua.entity.Category.builder()
                    .name("Trà Thượng Hạng Cầu Đất")
                    .description("Tuyển chọn búp trà đặc sản từ cao nguyên Cầu Đất")
                    .displayOrder(4)
                    .active(true)
                    .build());

            // Sản phẩm mẫu
            productRepository.save(com.ute.trasua.entity.Product.builder()
                    .productCode("SP-01")
                    .name("Trà Sữa Trân Châu Đường Đen")
                    .category(cat1)
                    .basePrice(new java.math.BigDecimal("35000"))
                    .imageUrl("https://images.unsplash.com/photo-1558857563-b37cf5a2a7e7?w=500")
                    .description("Cốt trà đen đậm đà hòa quyện sữa tươi thanh trùng và trân châu đường đen dẻo dai")
                    .status("ACTIVE")
                    .build());

            productRepository.save(com.ute.trasua.entity.Product.builder()
                    .productCode("SP-02")
                    .name("Trà Sữa Oolong Nướng Cầu Đất")
                    .category(cat1)
                    .basePrice(new java.math.BigDecimal("38000"))
                    .imageUrl("https://images.unsplash.com/photo-1541658016709-82535e94bc69?w=500")
                    .description("Vị trà Oolong sao lửa độc đáo hòa quyện lớp sữa béo ngậy thơm lừng")
                    .status("ACTIVE")
                    .build());

            productRepository.save(com.ute.trasua.entity.Product.builder()
                    .productCode("SP-03")
                    .name("Lục Trà Hoa Nhài Chanh Vàng")
                    .category(cat2)
                    .basePrice(new java.math.BigDecimal("32000"))
                    .imageUrl("https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?w=500")
                    .description("Hương hoa nhài ướp tự nhiên kết hợp vị chua ngọt sảng khoái của chanh vàng")
                    .status("ACTIVE")
                    .build());

            productRepository.save(com.ute.trasua.entity.Product.builder()
                    .productCode("SP-04")
                    .name("Trà Đào Cam Sả Tươi Mát")
                    .category(cat2)
                    .basePrice(new java.math.BigDecimal("39000"))
                    .imageUrl("https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=500")
                    .description("Miếng đào giòn tan kết hợp cam vàng mọng nước và hương sả tinh tế")
                    .status("ACTIVE")
                    .build());

            productRepository.save(com.ute.trasua.entity.Product.builder()
                    .productCode("SP-05")
                    .name("Matcha Uji Macchiato Nhật Bản")
                    .category(cat3)
                    .basePrice(new java.math.BigDecimal("45000"))
                    .imageUrl("https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=500")
                    .description("Bột trà xanh Uji Kyoto hảo hạng phủ lớp kem cheese béo mặn sánh mịn")
                    .status("ACTIVE")
                    .build());

            productRepository.save(com.ute.trasua.entity.Product.builder()
                    .productCode("SP-06")
                    .name("Trà Đen Macchiato Cổ Thụ")
                    .category(cat3)
                    .basePrice(new java.math.BigDecimal("40000"))
                    .imageUrl("https://images.unsplash.com/photo-1571934811356-5cc507423022?w=500")
                    .description("Cốt trà đen ủ lạnh 12 tiếng hòa quyện lớp foam phô mai ngọt dịu")
                    .status("ACTIVE")
                    .build());

            productRepository.save(com.ute.trasua.entity.Product.builder()
                    .productCode("SP-07")
                    .name("Trà Sen Vàng Long Nhãn")
                    .category(cat4)
                    .basePrice(new java.math.BigDecimal("42000"))
                    .imageUrl("https://images.unsplash.com/photo-1597481499750-3e6b22637e12?w=500")
                    .description("Hạt sen bùi thơm cùng long nhãn thanh ngọt trên nền trà Ô Long truyền thống")
                    .status("ACTIVE")
                    .build());

            productRepository.save(com.ute.trasua.entity.Product.builder()
                    .productCode("SP-08")
                    .name("Trà Thiết Quan Âm Tuyết Sơn")
                    .category(cat4)
                    .basePrice(new java.math.BigDecimal("48000"))
                    .imageUrl("https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=500")
                    .description("Dòng trà danh tiếng với hậu vị ngọt sâu lắng của vùng núi sương mù")
                    .status("ACTIVE")
                    .build());

            log.info(">>> Đã khởi tạo 4 danh mục và 8 sản phẩm trà sữa mẫu!");
        }

        syncMenuWithStorefront();
    }

    private void syncMenuWithStorefront() {
        // Đồng bộ danh mục sang phong cách Thiền Trà Wabi-Sabi
        java.util.List<com.ute.trasua.entity.Category> cats = categoryRepository.findAllByOrderByDisplayOrderAsc();
        if (cats.size() >= 3) {
            cats.get(0).setName("Trà Shan Tuyết & Nham Trà Độc Bản");
            cats.get(0).setDescription("Búp trà cổ thụ sương mù và nham trà khoáng chất");
            cats.get(1).setName("Trà Ướp Hoa Truyền Thống");
            cats.get(1).setDescription("Hòa quyện cốt trà thanh khiết cùng hoa tươi tự nhiên");
            cats.get(2).setName("Trà Oolong & Thảo Mộc Tĩnh Tâm");
            cats.get(2).setDescription("Hương vị an yên thư giãn tinh thần tuyệt đối");
            categoryRepository.saveAll(cats);
        }

        // Đồng bộ 4 Topping khớp với zenToppings trên Storefront
        java.util.List<com.ute.trasua.entity.Topping> toppings = toppingRepository.findAll();
        if (toppings.size() >= 4) {
            toppings.get(0).setName("Hạt sen Huế hầm đường phèn");
            toppings.get(0).setPrice(new java.math.BigDecimal("12000"));
            toppings.get(1).setName("Thạch sương sáo thảo mộc");
            toppings.get(1).setPrice(new java.math.BigDecimal("8000"));
            toppings.get(2).setName("Kem mây sữa mộc");
            toppings.get(2).setPrice(new java.math.BigDecimal("10000"));
            toppings.get(3).setName("Trân châu hoàng kim");
            toppings.get(3).setPrice(new java.math.BigDecimal("10000"));
            toppingRepository.saveAll(toppings);
        }

        // Đồng bộ 8 Món Trà khớp 100% với zenProducts trên Storefront
        java.util.List<com.ute.trasua.entity.Product> prods = productRepository.findAll();
        String[][] zenItems = {
            {"Bạch Hạc Shan Tuyết", "65000", "Búp trà cổ thụ 300 năm tuổi trên đỉnh Tà Xùa, hương khói nhẹ và hậu ngọt sâu lắng."},
            {"Thiết Quan Âm Ủ Lạnh", "58000", "Trà Thiết Quan Âm ủ lạnh 12h, lưu giữ trọn vẹn hương hoa lan thanh khiết."},
            {"Đại Hồng Bào Nham Trà", "70000", "Nham trà Vũ Di Sơn đượm khoáng chất, vị đậm đà và bền hương qua nhiều tuần nước."},
            {"Trà Sen Hồ Tây Thượng Hạng", "68000", "Trà xanh ướp trong bông sen Bách Diệp sớm mai tinh sương ngát hương thơm quý phái."},
            {"Trà Oolong Sữa Mộc Vân", "55000", "Trà ô long Kim Tuyên béo ngậy tự nhiên từ giống trà vùng cao Cầu Đất."},
            {"Lục Trà Hoa Nhài Cung Đình", "50000", "Hoa nhài hữu cơ thu hái lúc hoàng hôn hòa quyện lục trà thanh mảnh."},
            {"Hồng Trà Quế Hoa Hoàng Cung", "54000", "Hồng trà cổ thụ điểm xuyết hoa mộc vàng thơm dịu dàng ấm áp."},
            {"Trà Thảo Mộc Tĩnh Tâm", "52000", "Táo đỏ, kỷ tử, hoa cúc tuyết và cam thảo giúp thư giãn tinh thần tuyệt đối."}
        };

        for (int i = 0; i < Math.min(prods.size(), zenItems.length); i++) {
            com.ute.trasua.entity.Product p = prods.get(i);
            p.setName(zenItems[i][0]);
            p.setBasePrice(new java.math.BigDecimal(zenItems[i][1]));
            p.setDescription(zenItems[i][2]);
            productRepository.save(p);
        }
        log.info(">>> Đã đồng bộ 100% tên món trà và topping khớp với giao diện Storefront HTML!");

        // Khởi tạo Danh mục nguyên liệu pha chế & bao bì (Task 4.1 / Module 14)
        if (ingredientRepository.count() == 0) {
            java.util.List<com.ute.trasua.entity.Ingredient> rawMaterials = java.util.List.of(
                com.ute.trasua.entity.Ingredient.builder()
                    .code("NL001")
                    .name("Trà Đen Shan Tuyết Cổ Thụ")
                    .unit("Kg")
                    .referencePrice(new java.math.BigDecimal("350000"))
                    .description("Búp trà shan tuyết cổ thụ thu hái vùng núi cao Tà Xùa")
                    .status("ACTIVE")
                    .build(),
                com.ute.trasua.entity.Ingredient.builder()
                    .code("NL002")
                    .name("Lục Trà Ướp Hoa Nhài")
                    .unit("Kg")
                    .referencePrice(new java.math.BigDecimal("280000"))
                    .description("Trà xanh ướp hoa nhài hữu cơ hương thơm thanh tao")
                    .status("ACTIVE")
                    .build(),
                com.ute.trasua.entity.Ingredient.builder()
                    .code("NL003")
                    .name("Trà Ô Long Kim Tuyên Cầu Đất")
                    .unit("Kg")
                    .referencePrice(new java.math.BigDecimal("320000"))
                    .description("Trà ô long cao nguyên Cầu Đất hậu vị ngọt sâu")
                    .status("ACTIVE")
                    .build(),
                com.ute.trasua.entity.Ingredient.builder()
                    .code("NL004")
                    .name("Sữa Tươi Thanh Trùng Dalat Milk")
                    .unit("Lít")
                    .referencePrice(new java.math.BigDecimal("38000"))
                    .description("Sữa tươi thanh trùng 100% nguyên chất bảo quản lạnh 4°C")
                    .status("ACTIVE")
                    .build(),
                com.ute.trasua.entity.Ingredient.builder()
                    .code("NL005")
                    .name("Hạt Sen Tươi Huế Hầm Sẵn")
                    .unit("Kg")
                    .referencePrice(new java.math.BigDecimal("120000"))
                    .description("Hạt sen hồ Tịnh Tâm Huế bùi dẻo hầm đường phèn")
                    .status("ACTIVE")
                    .build(),
                com.ute.trasua.entity.Ingredient.builder()
                    .code("NL006")
                    .name("Thạch Sương Sáo Thảo Mộc Đài Loan")
                    .unit("Hộp")
                    .referencePrice(new java.math.BigDecimal("65000"))
                    .description("Thạch thảo mộc thanh nhiệt mềm mịn")
                    .status("ACTIVE")
                    .build(),
                com.ute.trasua.entity.Ingredient.builder()
                    .code("NL007")
                    .name("Kem Mây Sữa Béo (Macchiato Cream)")
                    .unit("Hộp")
                    .referencePrice(new java.math.BigDecimal("85000"))
                    .description("Kem sữa béo ngậy phủ mặt mộc mạc thơm dịu")
                    .status("ACTIVE")
                    .build(),
                com.ute.trasua.entity.Ingredient.builder()
                    .code("NL008")
                    .name("Trân Châu Hoàng Kim Nấu Sẵn")
                    .unit("Kg")
                    .referencePrice(new java.math.BigDecimal("45000"))
                    .description("Trân châu dẻo dai ướp mật ong tự nhiên")
                    .status("ACTIVE")
                    .build(),
                com.ute.trasua.entity.Ingredient.builder()
                    .code("NL009")
                    .name("Đường Phèn Tinh Khiết Quảng Ngãi")
                    .unit("Kg")
                    .referencePrice(new java.math.BigDecimal("32000"))
                    .description("Đường phèn vàng tự nhiên vị ngọt thanh tao")
                    .status("ACTIVE")
                    .build(),
                com.ute.trasua.entity.Ingredient.builder()
                    .code("NL010")
                    .name("Ly Giấy Tráng Men Sinh Học 500ml")
                    .unit("Cây")
                    .referencePrice(new java.math.BigDecimal("75000"))
                    .description("Cây 50 ly giấy cao cấp thân thiện môi trường")
                    .status("ACTIVE")
                    .build()
            );
            ingredientRepository.saveAll(rawMaterials);
            log.info(">>> Đã khởi tạo 10 danh mục nguyên liệu pha chế & bao bì chuẩn chuỗi!");

            // Khởi tạo tồn kho ban đầu cho chi nhánh Thủ Đức (CN-TD01)
            com.ute.trasua.entity.Branch tdBranch = branchRepository.findByBranchCode("CN-TD01").orElse(null);
            if (tdBranch != null) {
                java.util.List<com.ute.trasua.entity.BranchInventory> stockList = new java.util.ArrayList<>();
                for (com.ute.trasua.entity.Ingredient ing : rawMaterials) {
                    stockList.add(com.ute.trasua.entity.BranchInventory.builder()
                        .branch(tdBranch)
                        .ingredient(ing)
                        .stockQuantity(new java.math.BigDecimal("45.00"))
                        .minThreshold(new java.math.BigDecimal("10.00"))
                        .updatedAt(LocalDateTime.now())
                        .build());
                }
                branchInventoryRepository.saveAll(stockList);
                log.info(">>> Đã khởi tạo tồn kho nguyên liệu ban đầu cho CN-TD01!");

                // Tạo 1 Phiếu nhập kho mẫu BM2 (QLCN_BM2)
                if (importReceiptRepository.count() == 0) {
                    User manager = userRepository.findByUsername("manager1").orElse(null);
                    com.ute.trasua.entity.ImportReceipt receipt = com.ute.trasua.entity.ImportReceipt.builder()
                        .receiptCode("PNK-TD01-20261013-001")
                        .branch(tdBranch)
                        .importDate(LocalDateTime.now().minusDays(1))
                        .supplierType("CENTRAL_WAREHOUSE")
                        .supplierName("Tổng Kho Trung Tâm UTETea Sài Gòn")
                        .deliveredBy("Lê Văn Tải (Tài xế kho tổng)")
                        .receivedBy("Nguyễn Văn Quản Lý (QL CN Thủ Đức)")
                        .createdBy(manager)
                        .status("IMPORTED")
                        .note("Hàng hóa đóng gói quy chuẩn, nhiệt độ nguyên liệu lạnh 4°C, bao bì nguyên niêm phong.")
                        .build();

                    receipt.addDetail(com.ute.trasua.entity.ImportReceiptDetail.builder()
                        .ingredient(rawMaterials.get(0)) // Trà Đen Shan Tuyết
                        .quantity(new java.math.BigDecimal("20.00"))
                        .unitPrice(new java.math.BigDecimal("350000"))
                        .totalPrice(new java.math.BigDecimal("7000000"))
                        .expiryDate(java.time.LocalDate.now().plusMonths(12))
                        .note("Lô Shan Tuyết sấy vụ thu mới")
                        .build());

                    receipt.addDetail(com.ute.trasua.entity.ImportReceiptDetail.builder()
                        .ingredient(rawMaterials.get(3)) // Sữa Tươi Dalat Milk
                        .quantity(new java.math.BigDecimal("50.00"))
                        .unitPrice(new java.math.BigDecimal("38000"))
                        .totalPrice(new java.math.BigDecimal("1900000"))
                        .expiryDate(java.time.LocalDate.now().plusDays(14))
                        .note("Sữa tiệt trùng thanh mát")
                        .build());

                    receipt.addDetail(com.ute.trasua.entity.ImportReceiptDetail.builder()
                        .ingredient(rawMaterials.get(7)) // Trân Châu Hoàng Kim
                        .quantity(new java.math.BigDecimal("30.00"))
                        .unitPrice(new java.math.BigDecimal("45000"))
                        .totalPrice(new java.math.BigDecimal("1350000"))
                        .expiryDate(java.time.LocalDate.now().plusMonths(6))
                        .note("Trân châu hút chân không")
                        .build());

                    receipt.recalculateTotalAmount();
                    importReceiptRepository.save(receipt);
                    log.info(">>> Đã khởi tạo phiếu nhập kho mẫu BM2: {}", receipt.getReceiptCode());
                }
            }
        }

        // Khởi tạo các đơn hàng mẫu phục vụ báo cáo doanh thu & biểu đồ Chart.js (Module 15 / UC19, UC26)
        initSampleOrders();

        // Khởi tạo các chương trình khuyến mãi mẫu (Module 4 / UC18, UC24)
        initSamplePromotions();
    }

    private void initSampleOrders() {
        if (orderRepository.count() > 0) {
            return;
        }

        List<Branch> branches = branchRepository.findAll();
        List<Product> products = productRepository.findAll();

        if (branches.isEmpty() || products.isEmpty()) {
            return;
        }

        Branch branchTD = branches.stream().filter(b -> "CN-TD01".equals(b.getBranchCode())).findFirst().orElse(branches.get(0));
        Branch branchQ1 = branches.size() > 1 ? branches.get(1) : branchTD;
        Branch branchBT = branches.size() > 2 ? branches.get(2) : branchTD;

        LocalDateTime now = LocalDateTime.now();
        String[] paymentMethods = {"CASH", "VIETQR", "MOMO", "CREDIT_CARD"};

        List<Order> ordersToSave = new ArrayList<>();
        int orderIndex = 1;

        // Tạo chuỗi đơn hàng trong 7 ngày gần đây
        for (int dayOffset = 6; dayOffset >= 0; dayOffset--) {
            LocalDateTime dayTime = now.minusDays(dayOffset).withHour(9).withMinute(30);

            // Tạo các đơn cho Chi Nhánh Thủ Đức (4 đơn/ngày)
            for (int i = 0; i < 4; i++) {
                ordersToSave.add(createSampleOrder(
                        branchTD,
                        "ORD-TD-" + String.format("%04d", orderIndex++),
                        dayTime.plusHours(i * 3).plusMinutes(15),
                        paymentMethods[i % paymentMethods.length],
                        products,
                        i % 2 == 0 ? new BigDecimal("10000") : BigDecimal.ZERO
                ));
            }

            // Tạo các đơn cho Chi Nhánh Quận 1 (2-3 đơn/ngày)
            for (int i = 0; i < 3; i++) {
                ordersToSave.add(createSampleOrder(
                        branchQ1,
                        "ORD-Q1-" + String.format("%04d", orderIndex++),
                        dayTime.plusHours(i * 4).plusMinutes(35),
                        paymentMethods[(i + 1) % paymentMethods.length],
                        products,
                        i == 1 ? new BigDecimal("15000") : BigDecimal.ZERO
                ));
            }

            // Tạo các đơn cho Chi Nhánh Bình Thạnh (2 đơn/ngày)
            for (int i = 0; i < 2; i++) {
                ordersToSave.add(createSampleOrder(
                        branchBT,
                        "ORD-BT-" + String.format("%04d", orderIndex++),
                        dayTime.plusHours(i * 5).plusMinutes(20),
                        paymentMethods[(i + 2) % paymentMethods.length],
                        products,
                        BigDecimal.ZERO
                ));
            }
        }

        orderRepository.saveAll(ordersToSave);
        log.info(">>> Đã khởi tạo thành công {} đơn hàng mẫu phục vụ Báo Cáo Doanh Thu & Chart.js!", ordersToSave.size());
    }

    private Order createSampleOrder(Branch branch, String orderCode, LocalDateTime createdAt,
                                    String paymentMethod, List<Product> products, BigDecimal discount) {
        Order order = Order.builder()
                .orderCode(orderCode)
                .dailyQueueNumber((int) (Math.random() * 80) + 1)
                .orderType("POS_WALK_IN")
                .status("COMPLETED")
                .paymentMethod(paymentMethod)
                .paymentStatus("PAID")
                .cashierUsername("manager1")
                .branch(branch)
                .note("Đơn hàng tại quầy phục vụ khách")
                .createdAt(createdAt)
                .paidAt(createdAt.plusMinutes(2))
                .build();

        // Chọn ngẫu nhiên 1 đến 2 sản phẩm
        int numItems = (int) (Math.random() * 2) + 1;
        BigDecimal totalGross = BigDecimal.ZERO;

        for (int k = 0; k < numItems; k++) {
            Product p = products.get((int) (Math.random() * products.size()));
            int qty = (int) (Math.random() * 2) + 1;
            BigDecimal price = p.getBasePrice() != null ? p.getBasePrice() : new BigDecimal("35000");
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));
            totalGross = totalGross.add(subtotal);

            com.ute.trasua.entity.OrderDetail detail = com.ute.trasua.entity.OrderDetail.builder()
                    .order(order)
                    .product(p)
                    .productName(p.getName())
                    .quantity(qty)
                    .sugarLevel("70%")
                    .iceLevel("50%")
                    .unitPrice(price)
                    .subtotal(subtotal)
                    .build();

            order.getOrderDetails().add(detail);
        }

        order.setTotalAmount(totalGross);
        order.setDiscountAmount(discount != null ? discount : BigDecimal.ZERO);
        BigDecimal net = totalGross.subtract(order.getDiscountAmount());
        order.setFinalAmount(net.compareTo(BigDecimal.ZERO) > 0 ? net : totalGross);

        return order;
    }

    private void initSamplePromotions() {
        if (promotionRepository.count() > 0) {
            return;
        }

        Branch tdBranch = branchRepository.findAll().stream()
                .filter(b -> "CN-TD01".equals(b.getBranchCode()))
                .findFirst()
                .orElse(null);

        List<Promotion> samplePromotions = List.of(
                Promotion.builder()
                        .voucherCode("CHAOMUNG2026")
                        .title("Chào Đón Năm Mới 2026 Giảm 20%")
                        .description("Giảm ngay 20% tối đa 40.000đ cho đơn hàng trà sữa toàn hệ thống.")
                        .discountType("PERCENT")
                        .discountValue(new BigDecimal("20.00"))
                        .maxDiscountAmount(new BigDecimal("40000.00"))
                        .minOrderAmount(new BigDecimal("60000.00"))
                        .startDate(LocalDateTime.now().minusDays(5))
                        .endDate(LocalDateTime.now().plusMonths(2))
                        .totalUsageLimit(500)
                        .usedCount(12)
                        .usageLimitPerUser(2)
                        .scope("GLOBAL")
                        .branch(null)
                        .status("ACTIVE")
                        .build(),

                Promotion.builder()
                        .voucherCode("FREESHIP50K")
                        .title("Ưu Đãi Trợ Giá Ship 15.000đ")
                        .description("Hỗ trợ 15.000đ tiền vận chuyển cho đơn hàng từ 50.000đ trở lên.")
                        .discountType("FIXED_AMOUNT")
                        .discountValue(new BigDecimal("15000.00"))
                        .maxDiscountAmount(null)
                        .minOrderAmount(new BigDecimal("50000.00"))
                        .startDate(LocalDateTime.now().minusDays(3))
                        .endDate(LocalDateTime.now().plusDays(30))
                        .totalUsageLimit(200)
                        .usedCount(5)
                        .usageLimitPerUser(1)
                        .scope("GLOBAL")
                        .branch(null)
                        .status("ACTIVE")
                        .build(),

                Promotion.builder()
                        .voucherCode("TRIANKHACHHANG")
                        .title("Tri Ân Khách Hàng Thân Thiết Giảm 10%")
                        .description("Áp dụng cho mọi hóa đơn trà sữa từ 80.000đ toàn chuỗi.")
                        .discountType("PERCENT")
                        .discountValue(new BigDecimal("10.00"))
                        .maxDiscountAmount(new BigDecimal("25000.00"))
                        .minOrderAmount(new BigDecimal("80000.00"))
                        .startDate(LocalDateTime.now().minusDays(10))
                        .endDate(LocalDateTime.now().plusDays(45))
                        .totalUsageLimit(300)
                        .usedCount(28)
                        .usageLimitPerUser(3)
                        .scope("GLOBAL")
                        .branch(null)
                        .status("ACTIVE")
                        .build(),

                Promotion.builder()
                        .voucherCode("THUDUC10K")
                        .title("Mừng Khai Trương Điểm Bán Thủ Đức Giảm 10K")
                        .description("Ưu đãi độc quyền dành cho khách hàng đặt hàng tại chi nhánh Thủ Đức.")
                        .discountType("FIXED_AMOUNT")
                        .discountValue(new BigDecimal("10000.00"))
                        .maxDiscountAmount(null)
                        .minOrderAmount(new BigDecimal("40000.00"))
                        .startDate(LocalDateTime.now().minusDays(7))
                        .endDate(LocalDateTime.now().plusDays(25))
                        .totalUsageLimit(100)
                        .usedCount(18)
                        .usageLimitPerUser(1)
                        .scope("BRANCH")
                        .branch(tdBranch)
                        .status("ACTIVE")
                        .build()
        );

        promotionRepository.saveAll(samplePromotions);
        log.info(">>> Đã khởi tạo 4 chương trình khuyến mãi mẫu (3 Toàn chuỗi, 1 Chi nhánh Thủ Đức)!");
    }
}
