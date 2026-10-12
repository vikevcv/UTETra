package com.ute.trasua.config;

import com.ute.trasua.entity.PosAccount;
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
    private final com.ute.trasua.repository.CategoryRepository categoryRepository;
    private final com.ute.trasua.repository.ProductRepository productRepository;
    private final com.ute.trasua.repository.SizeRepository sizeRepository;
    private final com.ute.trasua.repository.ProductSizeRepository productSizeRepository;
    private final com.ute.trasua.repository.ToppingRepository toppingRepository;
    private final com.ute.trasua.repository.DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
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
                            .shift("MORNING")
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
                            .shift("AFTERNOON")
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
                            .shift("FULL_TIME")
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
    }
}
