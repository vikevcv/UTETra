package com.ute.trasua.service;

import com.ute.trasua.dto.ChiNhanhRequest;
import com.ute.trasua.dto.ChiNhanhResponse;
import com.ute.trasua.dto.UserResponse;
import com.ute.trasua.entity.ChiNhanh;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.ChiNhanhRepository;
import com.ute.trasua.repository.NhanVienRepository;
import com.ute.trasua.repository.TaiKhoanQuayRepository;
import com.ute.trasua.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service xử lý nghiệp vụ quản lý mạng lưới Chi nhánh (UC21).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChiNhanhService {

    private final ChiNhanhRepository chiNhanhRepository;
    private final NhanVienRepository nhanVienRepository;
    private final TaiKhoanQuayRepository taiKhoanQuayRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ChiNhanhResponse> getAllBranches() {
        return chiNhanhRepository.findAll().stream()
                .map(this::enrichToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChiNhanhResponse> getAllActiveBranches() {
        return chiNhanhRepository.findByActiveTrue().stream()
                .map(this::enrichToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ChiNhanhResponse> searchBranches(String keyword, Boolean active, Pageable pageable) {
        Page<ChiNhanh> page = chiNhanhRepository.searchChiNhanh(keyword, active, pageable);
        return page.map(this::enrichToResponse);
    }

    @Transactional(readOnly = true)
    public ChiNhanhResponse getBranchById(Long id) {
        ChiNhanh branch = getBranchEntityById(id);
        return enrichToResponse(branch);
    }

    @Transactional(readOnly = true)
    public ChiNhanh getBranchEntityById(Long id) {
        return chiNhanhRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh với ID: " + id));
    }

    @Transactional(readOnly = true)
    public ChiNhanhResponse getBranchByMa(String maChiNhanh) {
        ChiNhanh branch = chiNhanhRepository.findByMaChiNhanh(maChiNhanh)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh với mã: " + maChiNhanh));
        return enrichToResponse(branch);
    }

    @Transactional(readOnly = true)
    public Optional<ChiNhanhResponse> getBranchByManagerUsername(String username) {
        return chiNhanhRepository.findFirstByManagerUsername(username)
                .map(this::enrichToResponse);
    }

    @Transactional
    public ChiNhanhResponse createBranch(ChiNhanhRequest request) {
        if (request.getMaChiNhanh() == null || request.getMaChiNhanh().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã chi nhánh không được để trống!");
        }

        String ma = request.getMaChiNhanh().trim().toUpperCase();
        if (chiNhanhRepository.existsByMaChiNhanh(ma)) {
            throw new IllegalArgumentException("Mã chi nhánh [" + ma + "] đã tồn tại trong hệ thống!");
        }

        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId()).orElse(null);
        }

        ChiNhanh branch = ChiNhanh.builder()
                .maChiNhanh(ma)
                .tenChiNhanh(request.getTenChiNhanh().trim())
                .diaChi(request.getDiaChi().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .hotline(request.getHotline() != null ? request.getHotline().trim() : null)
                .gioMoCua(request.getGioMoCua() != null ? request.getGioMoCua().trim() : "07:00")
                .gioDongCua(request.getGioDongCua() != null ? request.getGioDongCua().trim() : "22:30")
                .active(request.isActive())
                .manager(manager)
                .build();

        ChiNhanh saved = chiNhanhRepository.save(branch);
        log.info(">>> Đã tạo mới chi nhánh: [{}] {}", saved.getMaChiNhanh(), saved.getTenChiNhanh());
        return enrichToResponse(saved);
    }

    @Transactional
    public ChiNhanhResponse updateBranch(Long id, ChiNhanhRequest request) {
        ChiNhanh branch = getBranchEntityById(id);

        if (request.getMaChiNhanh() != null && !request.getMaChiNhanh().trim().isEmpty()) {
            String ma = request.getMaChiNhanh().trim().toUpperCase();
            if (chiNhanhRepository.existsByMaChiNhanhAndIdNot(ma, id)) {
                throw new IllegalArgumentException("Mã chi nhánh [" + ma + "] đã thuộc về chi nhánh khác!");
            }
            branch.setMaChiNhanh(ma);
        }

        if (request.getTenChiNhanh() != null) branch.setTenChiNhanh(request.getTenChiNhanh().trim());
        if (request.getDiaChi() != null) branch.setDiaChi(request.getDiaChi().trim());
        if (request.getLatitude() != null) branch.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) branch.setLongitude(request.getLongitude());
        if (request.getHotline() != null) branch.setHotline(request.getHotline().trim());
        if (request.getGioMoCua() != null) branch.setGioMoCua(request.getGioMoCua().trim());
        if (request.getGioDongCua() != null) branch.setGioDongCua(request.getGioDongCua().trim());
        branch.setActive(request.isActive());

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId()).orElse(null);
            branch.setManager(manager);
        } else {
            branch.setManager(null);
        }

        ChiNhanh updated = chiNhanhRepository.save(branch);
        log.info(">>> Đã cập nhật chi nhánh ID {}: [{}] {}", id, updated.getMaChiNhanh(), updated.getTenChiNhanh());
        return enrichToResponse(updated);
    }

    @Transactional
    public boolean toggleBranchStatus(Long id) {
        ChiNhanh branch = getBranchEntityById(id);
        boolean newStatus = !branch.isActive();
        branch.setActive(newStatus);
        chiNhanhRepository.save(branch);
        log.info(">>> Đã đổi trạng thái chi nhánh [{}] thành: {}", branch.getMaChiNhanh(), newStatus ? "Hoạt động" : "Tạm dừng");
        return newStatus;
    }

    @Transactional
    public void deleteBranch(Long id) {
        ChiNhanh branch = getBranchEntityById(id);
        long countNv = nhanVienRepository.countByChiNhanhId(id);
        if (countNv > 0) {
            // Không xóa cứng nếu còn nhân sự, tự động chuyển về ngưng hoạt động
            branch.setActive(false);
            chiNhanhRepository.save(branch);
            log.warn(">>> Chi nhánh ID {} còn {} nhân sự liên kết -> Đã chuyển trạng thái sang ngưng hoạt động thay vì xóa cứng.", id, countNv);
        } else {
            chiNhanhRepository.delete(branch);
            log.info(">>> Đã xóa chi nhánh ID {} khỏi hệ thống.", id);
        }
    }

    /**
     * Tìm chi nhánh gần nhất đang hoạt động dựa trên toạ độ GPS của khách hàng/tài xế.
     */
    @Transactional(readOnly = true)
    public ChiNhanhResponse findNearestBranch(Double userLat, Double userLng) {
        List<ChiNhanhResponse> activeBranches = getAllActiveBranches();
        if (activeBranches.isEmpty() || userLat == null || userLng == null) {
            return null;
        }

        return activeBranches.stream()
                .filter(b -> b.getLatitude() != null && b.getLongitude() != null)
                .min(Comparator.comparingDouble(b -> b.calculateDistanceKm(userLat, userLng)))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getEligibleManagers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("ROLE_MANAGER") || u.getRoles().contains("ROLE_ADMIN"))
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countActiveBranches() {
        return chiNhanhRepository.countByActiveTrue();
    }

    @Transactional(readOnly = true)
    public long countTotalBranches() {
        return chiNhanhRepository.count();
    }

    private ChiNhanhResponse enrichToResponse(ChiNhanh c) {
        long totalEmp = nhanVienRepository.countByChiNhanhId(c.getId());
        long activeEmp = nhanVienRepository.countByChiNhanhIdAndTrangThai(c.getId(), "DANG_LAM");
        long totalPos = taiKhoanQuayRepository.countByChiNhanhId(c.getId());
        return ChiNhanhResponse.fromEntity(c, totalEmp, activeEmp, totalPos);
    }
}
