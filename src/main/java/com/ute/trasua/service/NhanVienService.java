package com.ute.trasua.service;

import com.ute.trasua.dto.NhanVienRequest;
import com.ute.trasua.dto.NhanVienResponse;
import com.ute.trasua.dto.TaiKhoanQuayRequest;
import com.ute.trasua.dto.TaiKhoanQuayResponse;
import com.ute.trasua.entity.ChiNhanh;
import com.ute.trasua.entity.NhanVien;
import com.ute.trasua.entity.TaiKhoanQuay;
import com.ute.trasua.repository.ChiNhanhRepository;
import com.ute.trasua.repository.NhanVienRepository;
import com.ute.trasua.repository.TaiKhoanQuayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý nghiệp vụ Hồ sơ nhân sự và Thiết lập tài khoản quầy tại Chi nhánh (UC16).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NhanVienService {

    private final NhanVienRepository nhanVienRepository;
    private final ChiNhanhRepository chiNhanhRepository;
    private final TaiKhoanQuayRepository taiKhoanQuayRepository;
    private final PasswordEncoder passwordEncoder;

    // ==========================================
    // QUẢN LÝ HỒ SƠ NHÂN VIÊN (UC16)
    // ==========================================

    @Transactional(readOnly = true)
    public NhanVienResponse getNhanVienById(Long id) {
        return NhanVienResponse.fromEntity(getNhanVienEntityById(id));
    }

    @Transactional(readOnly = true)
    public NhanVien getNhanVienEntityById(Long id) {
        return nhanVienRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<NhanVienResponse> searchNhanVien(Long chiNhanhId,
                                                String keyword,
                                                String viTri,
                                                String caLamViec,
                                                String trangThai,
                                                Pageable pageable) {
        Page<NhanVien> page = nhanVienRepository.searchNhanVien(chiNhanhId, keyword, viTri, caLamViec, trangThai, pageable);
        return page.map(NhanVienResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<NhanVienResponse> getAllByChiNhanh(Long chiNhanhId) {
        return nhanVienRepository.findByChiNhanhId(chiNhanhId).stream()
                .map(NhanVienResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public NhanVienResponse createNhanVien(NhanVienRequest request) {
        if (request.getMaNhanVien() == null || request.getMaNhanVien().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống!");
        }

        String ma = request.getMaNhanVien().trim().toUpperCase();
        if (nhanVienRepository.existsByMaNhanVien(ma)) {
            throw new IllegalArgumentException("Mã nhân viên [" + ma + "] đã tồn tại!");
        }

        if (request.getChiNhanhId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn chi nhánh làm việc cho nhân viên!");
        }

        ChiNhanh chiNhanh = chiNhanhRepository.findById(request.getChiNhanhId())
                .orElseThrow(() -> new IllegalArgumentException("Chi nhánh chỉ định không tồn tại!"));

        NhanVien nv = NhanVien.builder()
                .maNhanVien(ma)
                .hoTen(request.getHoTen().trim())
                .soDienThoai(request.getSoDienThoai() != null ? request.getSoDienThoai().trim() : null)
                .cccd(request.getCccd() != null ? request.getCccd().trim() : null)
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .viTri(request.getViTri() != null ? request.getViTri().trim() : "PHA_CHE")
                .caLamViec(request.getCaLamViec() != null ? request.getCaLamViec().trim() : "CA_SANG")
                .mucLuong(request.getMucLuong() != null ? request.getMucLuong() : 0.0)
                .trangThai(request.getTrangThai() != null ? request.getTrangThai().trim() : "DANG_LAM")
                .ngayVaoLam(request.getNgayVaoLam() != null ? request.getNgayVaoLam() : java.time.LocalDate.now())
                .chiNhanh(chiNhanh)
                .build();

        NhanVien saved = nhanVienRepository.save(nv);
        log.info(">>> Đã tiếp nhận nhân viên mới: [{}] {} - Chi nhánh: {}", saved.getMaNhanVien(), saved.getHoTen(), chiNhanh.getTenChiNhanh());
        return NhanVienResponse.fromEntity(saved);
    }

    @Transactional
    public NhanVienResponse updateNhanVien(Long id, NhanVienRequest request) {
        NhanVien nv = getNhanVienEntityById(id);

        if (request.getMaNhanVien() != null && !request.getMaNhanVien().trim().isEmpty()) {
            String ma = request.getMaNhanVien().trim().toUpperCase();
            if (nhanVienRepository.existsByMaNhanVienAndIdNot(ma, id)) {
                throw new IllegalArgumentException("Mã nhân viên [" + ma + "] đã thuộc về nhân viên khác!");
            }
            nv.setMaNhanVien(ma);
        }

        if (request.getHoTen() != null) nv.setHoTen(request.getHoTen().trim());
        if (request.getSoDienThoai() != null) nv.setSoDienThoai(request.getSoDienThoai().trim());
        if (request.getCccd() != null) nv.setCccd(request.getCccd().trim());
        if (request.getEmail() != null) nv.setEmail(request.getEmail().trim());
        if (request.getViTri() != null) nv.setViTri(request.getViTri().trim());
        if (request.getCaLamViec() != null) nv.setCaLamViec(request.getCaLamViec().trim());
        if (request.getMucLuong() != null) nv.setMucLuong(request.getMucLuong());
        if (request.getTrangThai() != null) nv.setTrangThai(request.getTrangThai().trim());
        if (request.getNgayVaoLam() != null) nv.setNgayVaoLam(request.getNgayVaoLam());

        if (request.getChiNhanhId() != null && !request.getChiNhanhId().equals(nv.getChiNhanh().getId())) {
            ChiNhanh newCn = chiNhanhRepository.findById(request.getChiNhanhId())
                    .orElseThrow(() -> new IllegalArgumentException("Chi nhánh chuyển đến không tồn tại!"));
            nv.setChiNhanh(newCn);
            log.info(">>> Điều chuyển nhân viên ID {} sang chi nhánh {}", id, newCn.getTenChiNhanh());
        }

        NhanVien updated = nhanVienRepository.save(nv);
        return NhanVienResponse.fromEntity(updated);
    }

    @Transactional
    public void updateTrangThai(Long id, String trangThai) {
        NhanVien nv = getNhanVienEntityById(id);
        nv.setTrangThai(trangThai);
        nhanVienRepository.save(nv);
        log.info(">>> Cập nhật trạng thái nhân viên [{}] thành: {}", nv.getMaNhanVien(), trangThai);
    }

    @Transactional
    public void deleteNhanVien(Long id) {
        NhanVien nv = getNhanVienEntityById(id);
        nhanVienRepository.delete(nv);
        log.info(">>> Đã xóa hồ sơ nhân viên ID {}: [{}] {}", id, nv.getMaNhanVien(), nv.getHoTen());
    }

    @Transactional(readOnly = true)
    public long countByChiNhanh(Long chiNhanhId) {
        return nhanVienRepository.countByChiNhanhId(chiNhanhId);
    }

    @Transactional(readOnly = true)
    public long countActiveByChiNhanh(Long chiNhanhId) {
        return nhanVienRepository.countByChiNhanhIdAndTrangThai(chiNhanhId, "DANG_LAM");
    }

    // ==========================================
    // QUẢN LÝ TÀI KHOẢN QUẦY POS (UC16)
    // ==========================================

    @Transactional(readOnly = true)
    public List<TaiKhoanQuayResponse> getTaiKhoanQuayByChiNhanh(Long chiNhanhId) {
        return taiKhoanQuayRepository.findByChiNhanhId(chiNhanhId).stream()
                .map(TaiKhoanQuayResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaiKhoanQuayResponse createTaiKhoanQuay(TaiKhoanQuayRequest request) {
        if (request.getTenDangNhap() == null || request.getTenDangNhap().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập tài khoản quầy không được để trống!");
        }

        String username = request.getTenDangNhap().trim().toLowerCase();
        if (taiKhoanQuayRepository.existsByTenDangNhap(username)) {
            throw new IllegalArgumentException("Tên đăng nhập [" + username + "] đã được sử dụng!");
        }

        if (request.getChiNhanhId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn chi nhánh cho quầy bán!");
        }

        ChiNhanh chiNhanh = chiNhanhRepository.findById(request.getChiNhanhId())
                .orElseThrow(() -> new IllegalArgumentException("Chi nhánh không tồn tại!"));

        String rawPassword = request.getMatKhau() != null && !request.getMatKhau().trim().isEmpty()
                ? request.getMatKhau().trim() : "Pos@123";

        TaiKhoanQuay tk = TaiKhoanQuay.builder()
                .tenQuay(request.getTenQuay() != null ? request.getTenQuay().trim() : "Quầy Bán Hàng")
                .tenDangNhap(username)
                .matKhau(passwordEncoder.encode(rawPassword))
                .active(request.isActive())
                .ghiChu(request.getGhiChu() != null ? request.getGhiChu().trim() : null)
                .chiNhanh(chiNhanh)
                .build();

        TaiKhoanQuay saved = taiKhoanQuayRepository.save(tk);
        log.info(">>> Đã thiết lập tài khoản quầy POS: [{}] cho chi nhánh {}", saved.getTenDangNhap(), chiNhanh.getTenChiNhanh());
        return TaiKhoanQuayResponse.fromEntity(saved);
    }

    @Transactional
    public boolean toggleTaiKhoanQuay(Long id) {
        TaiKhoanQuay tk = taiKhoanQuayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản quầy với ID: " + id));
        boolean newStatus = !tk.isActive();
        tk.setActive(newStatus);
        taiKhoanQuayRepository.save(tk);
        log.info(">>> Đổi trạng thái tài khoản quầy [{}] thành: {}", tk.getTenDangNhap(), newStatus ? "Hoạt động" : "Tạm khóa");
        return newStatus;
    }

    @Transactional
    public void deleteTaiKhoanQuay(Long id) {
        TaiKhoanQuay tk = taiKhoanQuayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản quầy với ID: " + id));
        taiKhoanQuayRepository.delete(tk);
        log.info(">>> Đã xóa tài khoản quầy ID {}: [{}]", id, tk.getTenDangNhap());
    }
}
