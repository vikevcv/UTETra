package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.*;
import com.ute.trasua.service.ChiNhanhService;
import com.ute.trasua.service.NhanVienService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller phục vụ Quản lý chi nhánh (QLCN) quản lý hồ sơ nhân viên,
 * phân công ca làm việc và thiết lập tài khoản quầy POS (Module 12 - UC16).
 */
@Controller
@RequestMapping("/branch/employees")
@RequiredArgsConstructor
@Slf4j
public class NhanVienController {

    private final NhanVienService nhanVienService;
    private final ChiNhanhService chiNhanhService;

    /**
     * Màn hình danh sách nhân sự chi nhánh theo ca trực & Quản lý quầy POS (UC16).
     */
    @GetMapping
    public String listEmployees(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "viTri", required = false) String viTri,
            @RequestParam(value = "caLamViec", required = false) String caLamViec,
            @RequestParam(value = "trangThai", required = false) String trangThai,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication authentication,
            Model model
    ) {
        // Xác định chi nhánh làm việc hiện tại
        ChiNhanhResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long targetBranchId = (currentBranch != null) ? currentBranch.getId() : null;

        Page<NhanVienResponse> employeesPage = nhanVienService.searchNhanVien(
                targetBranchId,
                keyword,
                viTri,
                caLamViec,
                trangThai,
                PageRequest.of(page, size, Sort.by("id").descending())
        );

        // Danh sách tài khoản quầy POS (UC16)
        List<TaiKhoanQuayResponse> posAccounts = List.of();
        if (targetBranchId != null) {
            posAccounts = nhanVienService.getTaiKhoanQuayByChiNhanh(targetBranchId);
        }

        // Thống kê nhân sự
        long totalEmp = (targetBranchId != null) ? nhanVienService.countByChiNhanh(targetBranchId) : 0;
        long activeEmp = (targetBranchId != null) ? nhanVienService.countActiveByChiNhanh(targetBranchId) : 0;

        List<ChiNhanhResponse> allBranches = chiNhanhService.getAllBranches();

        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("currentBranchName", currentBranch != null ? currentBranch.getTenChiNhanh() : "Toàn Chuỗi");
        model.addAttribute("branchId", targetBranchId);
        model.addAttribute("employeesPage", employeesPage);
        model.addAttribute("posAccounts", posAccounts);
        model.addAttribute("totalEmp", totalEmp);
        model.addAttribute("activeEmp", activeEmp);
        model.addAttribute("allBranches", allBranches);
        model.addAttribute("keyword", keyword);
        model.addAttribute("viTri", viTri);
        model.addAttribute("caLamViec", caLamViec);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("pageTitle", "Quản Lý Nhân Sự & Ca Trực — UTETra");

        return "branch/employees/list";
    }

    /**
     * Mở form tiếp nhận nhân viên mới.
     */
    @GetMapping("/new")
    public String showCreateForm(
            @RequestParam(value = "branchId", required = false) Long branchId,
            Authentication authentication,
            Model model
    ) {
        ChiNhanhResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long defaultBranchId = (currentBranch != null) ? currentBranch.getId() : null;

        NhanVienRequest employee = NhanVienRequest.builder()
                .chiNhanhId(defaultBranchId)
                .viTri("PHA_CHE")
                .caLamViec("CA_SANG")
                .trangThai("DANG_LAM")
                .ngayVaoLam(java.time.LocalDate.now())
                .build();

        List<ChiNhanhResponse> allBranches = chiNhanhService.getAllBranches();

        model.addAttribute("employee", employee);
        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("currentBranchName", currentBranch != null ? currentBranch.getTenChiNhanh() : "Toàn Chuỗi");
        model.addAttribute("allBranches", allBranches);
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Tiếp Nhận Nhân Viên Mới — UTETra");

        return "branch/employees/form";
    }

    /**
     * Mở form cập nhật hồ sơ & ca làm việc của nhân viên.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            NhanVienResponse resp = nhanVienService.getNhanVienById(id);

            NhanVienRequest employee = NhanVienRequest.builder()
                    .id(resp.getId())
                    .maNhanVien(resp.getMaNhanVien())
                    .hoTen(resp.getHoTen())
                    .soDienThoai(resp.getSoDienThoai())
                    .cccd(resp.getCccd())
                    .email(resp.getEmail())
                    .viTri(resp.getViTri())
                    .caLamViec(resp.getCaLamViec())
                    .mucLuong(resp.getMucLuong())
                    .trangThai(resp.getTrangThai())
                    .ngayVaoLam(resp.getNgayVaoLam())
                    .chiNhanhId(resp.getChiNhanhId())
                    .build();

            ChiNhanhResponse currentBranch = null;
            if (resp.getChiNhanhId() != null) {
                currentBranch = chiNhanhService.getBranchById(resp.getChiNhanhId());
            }

            List<ChiNhanhResponse> allBranches = chiNhanhService.getAllBranches();

            model.addAttribute("employee", employee);
            model.addAttribute("currentBranch", currentBranch);
            model.addAttribute("currentBranchName", currentBranch != null ? currentBranch.getTenChiNhanh() : "Toàn Chuỗi");
            model.addAttribute("allBranches", allBranches);
            model.addAttribute("isEdit", true);
            model.addAttribute("employeeId", id);
            model.addAttribute("pageTitle", "Cập Nhật Nhân Viên: " + resp.getHoTen() + " — UTETra");

            return "branch/employees/form";
        } catch (Exception e) {
            log.error("Lỗi khi mở form sửa nhân viên ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/branch/employees";
        }
    }

    /**
     * Lưu thông tin nhân viên (Tạo mới hoặc Cập nhật).
     */
    @PostMapping("/save")
    public String saveEmployee(
            @ModelAttribute NhanVienRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (request.getId() == null) {
                NhanVienResponse created = nhanVienService.createNhanVien(request);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã tiếp nhận nhân viên mới: [" + created.getMaNhanVien() + "] " + created.getHoTen());
            } else {
                NhanVienResponse updated = nhanVienService.updateNhanVien(request.getId(), request);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã cập nhật hồ sơ nhân viên: [" + updated.getMaNhanVien() + "] " + updated.getHoTen());
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (request.getId() != null) {
                return "redirect:/branch/employees/edit/" + request.getId();
            } else {
                return "redirect:/branch/employees/new?branchId=" + request.getChiNhanhId();
            }
        } catch (Exception e) {
            log.error("Lỗi khi lưu nhân viên: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
        }

        return "redirect:/branch/employees" + (request.getChiNhanhId() != null ? "?branchId=" + request.getChiNhanhId() : "");
    }

    /**
     * Thay đổi trạng thái làm việc (Đang làm / Tạm nghỉ / Đã nghỉ việc).
     */
    @PostMapping("/{id}/update-status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String trangThai,
            @RequestParam(required = false) Long branchId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            nhanVienService.updateTrangThai(id, trangThai);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái nhân sự thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/employees" + (branchId != null ? "?branchId=" + branchId : "");
    }

    /**
     * Xóa hồ sơ nhân viên.
     */
    @PostMapping("/{id}/delete")
    public String deleteEmployee(
            @PathVariable Long id,
            @RequestParam(required = false) Long branchId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            nhanVienService.deleteNhanVien(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa hồ sơ nhân viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/employees" + (branchId != null ? "?branchId=" + branchId : "");
    }

    // ==========================================
    // THIẾT LẬP TÀI KHOẢN QUẦY POS (UC16)
    // ==========================================

    @PostMapping("/pos/create")
    public String createPosAccount(
            @ModelAttribute TaiKhoanQuayRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            TaiKhoanQuayResponse created = nhanVienService.createTaiKhoanQuay(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã thiết lập tài khoản quầy POS thành công: " + created.getTenDangNhap());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/employees" + (request.getChiNhanhId() != null ? "?branchId=" + request.getChiNhanhId() : "");
    }

    @PostMapping("/pos/{id}/toggle")
    public String togglePosAccount(
            @PathVariable Long id,
            @RequestParam(required = false) Long branchId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean active = nhanVienService.toggleTaiKhoanQuay(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã đổi trạng thái tài khoản quầy thành: " + (active ? "HOẠT ĐỘNG" : "TẠM KHÓA"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/employees" + (branchId != null ? "?branchId=" + branchId : "");
    }

    @PostMapping("/pos/{id}/delete")
    public String deletePosAccount(
            @PathVariable Long id,
            @RequestParam(required = false) Long branchId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            nhanVienService.deleteTaiKhoanQuay(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài khoản quầy POS!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/employees" + (branchId != null ? "?branchId=" + branchId : "");
    }

    /**
     * Hàm tiện ích xác định chi nhánh đang thao tác dựa trên đăng nhập hoặc param.
     */
    private ChiNhanhResponse resolveCurrentBranch(Long requestedBranchId, Authentication authentication) {
        if (requestedBranchId != null) {
            try {
                return chiNhanhService.getBranchById(requestedBranchId);
            } catch (Exception ignored) {}
        }

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            var branchOpt = chiNhanhService.getBranchByManagerUsername(username);
            if (branchOpt.isPresent()) {
                return branchOpt.get();
            }
        }

        // Mặc định lấy chi nhánh đầu tiên nếu chưa gán cụ thể
        List<ChiNhanhResponse> all = chiNhanhService.getAllBranches();
        return all.isEmpty() ? null : all.get(0);
    }
}
