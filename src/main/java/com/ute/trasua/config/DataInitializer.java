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
    private final com.ute.trasua.repository.ChiNhanhRepository chiNhanhRepository;
    private final com.ute.trasua.repository.NhanVienRepository nhanVienRepository;
    private final com.ute.trasua.repository.TaiKhoanQuayRepository taiKhoanQuayRepository;
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
        if (!chiNhanhRepository.existsByMaChiNhanh("CN-TD01")) {
            User manager1 = userRepository.findByUsername("manager1").orElse(null);
            com.ute.trasua.entity.ChiNhanh cnThuDuc = com.ute.trasua.entity.ChiNhanh.builder()
                    .maChiNhanh("CN-TD01")
                    .tenChiNhanh("UTETea Cơ Sở Thủ Đức (Đại Học SPKT)")
                    .diaChi("Số 1 Võ Văn Ngân, P. Linh Chiểu, TP. Thủ Đức, TP. Hồ Chí Minh")
                    .latitude(10.85072)
                    .longitude(106.77196)
                    .hotline("028.3896.8641")
                    .gioMoCua("07:00")
                    .gioDongCua("22:30")
                    .active(true)
                    .manager(manager1)
                    .createdAt(LocalDateTime.now().minusDays(30))
                    .build();
            cnThuDuc = chiNhanhRepository.save(cnThuDuc);
            log.info(">>> Đã khởi tạo Chi Nhánh CN-TD01: {}", cnThuDuc.getTenChiNhanh());

            // Nhân sự mẫu cho CN-TD01 (UC16)
            if (!nhanVienRepository.existsByMaNhanVien("NV001")) {
                nhanVienRepository.save(com.ute.trasua.entity.NhanVien.builder()
                        .maNhanVien("NV001")
                        .hoTen("Nguyễn Thị Hồng")
                        .soDienThoai("0911223344")
                        .cccd("079198001234")
                        .email("hong.nguyen@utetra.vn")
                        .viTri("PHA_CHE")
                        .caLamViec("CA_SANG")
                        .mucLuong(7500000.0)
                        .trangThai("DANG_LAM")
                        .ngayVaoLam(java.time.LocalDate.now().minusMonths(6))
                        .chiNhanh(cnThuDuc)
                        .build());
            }

            if (!nhanVienRepository.existsByMaNhanVien("NV002")) {
                nhanVienRepository.save(com.ute.trasua.entity.NhanVien.builder()
                        .maNhanVien("NV002")
                        .hoTen("Trần Minh Nhật")
                        .soDienThoai("0922334455")
                        .cccd("079200005678")
                        .email("nhat.tran@utetra.vn")
                        .viTri("THU_NGAN")
                        .caLamViec("CA_CHIEU")
                        .mucLuong(6800000.0)
                        .trangThai("DANG_LAM")
                        .ngayVaoLam(java.time.LocalDate.now().minusMonths(4))
                        .chiNhanh(cnThuDuc)
                        .build());
            }

            if (!nhanVienRepository.existsByMaNhanVien("NV003")) {
                nhanVienRepository.save(com.ute.trasua.entity.NhanVien.builder()
                        .maNhanVien("NV003")
                        .hoTen("Lê Hoàng Nam")
                        .soDienThoai("0933445566")
                        .cccd("079201009988")
                        .email("nam.le@utetra.vn")
                        .viTri("PHUC_VU")
                        .caLamViec("CA_TOI")
                        .mucLuong(5500000.0)
                        .trangThai("DANG_LAM")
                        .ngayVaoLam(java.time.LocalDate.now().minusMonths(2))
                        .chiNhanh(cnThuDuc)
                        .build());
            }

            if (!nhanVienRepository.existsByMaNhanVien("NV004")) {
                nhanVienRepository.save(com.ute.trasua.entity.NhanVien.builder()
                        .maNhanVien("NV004")
                        .hoTen("Võ Thị Bích Thảo")
                        .soDienThoai("0944556677")
                        .cccd("079199003322")
                        .email("thao.vo@utetra.vn")
                        .viTri("PHA_CHE")
                        .caLamViec("FULL_TIME")
                        .mucLuong(8500000.0)
                        .trangThai("DANG_LAM")
                        .ngayVaoLam(java.time.LocalDate.now().minusMonths(8))
                        .chiNhanh(cnThuDuc)
                        .build());
            }

            // Tài khoản quầy POS (UC16)
            if (!taiKhoanQuayRepository.existsByTenDangNhap("pos_td01")) {
                taiKhoanQuayRepository.save(com.ute.trasua.entity.TaiKhoanQuay.builder()
                        .tenQuay("Quầy Thu Ngân 01 - Thủ Đức")
                        .tenDangNhap("pos_td01")
                        .matKhau(passwordEncoder.encode("Pos@123"))
                        .active(true)
                        .ghiChu("Máy POS quầy mặt tiền sảnh chính")
                        .chiNhanh(cnThuDuc)
                        .build());
            }
        }

        if (!chiNhanhRepository.existsByMaChiNhanh("CN-Q01")) {
            User admin = userRepository.findByUsername("admin").orElse(null);
            com.ute.trasua.entity.ChiNhanh cnQuan1 = com.ute.trasua.entity.ChiNhanh.builder()
                    .maChiNhanh("CN-Q01")
                    .tenChiNhanh("UTETea Flagship Quận 1 (Phố Đi Bộ)")
                    .diaChi("68 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh")
                    .latitude(10.77431)
                    .longitude(106.70322)
                    .hotline("028.3822.9999")
                    .gioMoCua("07:30")
                    .gioDongCua("23:00")
                    .active(true)
                    .manager(admin)
                    .createdAt(LocalDateTime.now().minusDays(25))
                    .build();
            cnQuan1 = chiNhanhRepository.save(cnQuan1);
            log.info(">>> Đã khởi tạo Chi Nhánh CN-Q01: {}", cnQuan1.getTenChiNhanh());

            if (!taiKhoanQuayRepository.existsByTenDangNhap("pos_q101")) {
                taiKhoanQuayRepository.save(com.ute.trasua.entity.TaiKhoanQuay.builder()
                        .tenQuay("Quầy Thu Ngân 01 - Quận 1")
                        .tenDangNhap("pos_q101")
                        .matKhau(passwordEncoder.encode("Pos@123"))
                        .active(true)
                        .ghiChu("Máy POS quầy sảnh tầng trệt")
                        .chiNhanh(cnQuan1)
                        .build());
            }
        }
    }
}
