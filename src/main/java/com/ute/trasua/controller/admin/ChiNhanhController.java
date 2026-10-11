package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.ChiNhanhRequest;
import com.ute.trasua.dto.ChiNhanhResponse;
import com.ute.trasua.dto.UserResponse;
import com.ute.trasua.service.ChiNhanhService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller phục vụ Chủ chuỗi quản lý mạng lưới Chi nhánh (Module 12 - UC21).
 */
@Controller
@RequestMapping("/admin/branches")
@RequiredArgsConstructor
@Slf4j
public class ChiNhanhController {

    private final ChiNhanhService chiNhanhService;

    /**
     * Màn hình danh sách cơ sở chi nhánh toàn hệ thống kèm bản đồ mạng lưới (UC21).
     */
    @GetMapping
    public String listBranches(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            Model model
    ) {
        Page<ChiNhanhResponse> branchesPage = chiNhanhService.searchBranches(
                keyword,
                active,
                PageRequest.of(page, size, Sort.by("id").descending())
        );

        long totalBranches = chiNhanhService.countTotalBranches();
        long activeBranches = chiNhanhService.countActiveBranches();
        long pausedBranches = totalBranches - activeBranches;

        // Lấy tất cả chi nhánh để render các điểm pin trên bản đồ mạng lưới chuỗi
        List<ChiNhanhResponse> allBranches = chiNhanhService.getAllBranches();

        model.addAttribute("branchesPage", branchesPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("active", active);
        model.addAttribute("totalBranches", totalBranches);
        model.addAttribute("activeBranches", activeBranches);
        model.addAttribute("pausedBranches", pausedBranches);
        model.addAttribute("allBranches", allBranches);
        model.addAttribute("pageTitle", "Mạng Lưới Chi Nhánh & Điểm Bán — UTETra");

        return "admin/branches/list";
    }

    /**
     * Form thêm mới chi nhánh tích hợp bản đồ chọn tọa độ GPS.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        ChiNhanhRequest branch = ChiNhanhRequest.builder()
                .gioMoCua("07:00")
                .gioDongCua("22:30")
                .active(true)
                .latitude(10.85072) // Tọa độ mặc định: ĐH SPKT TP.HCM (Thủ Đức)
                .longitude(106.77196)
                .build();

        List<UserResponse> managers = chiNhanhService.getEligibleManagers();

        model.addAttribute("branch", branch);
        model.addAttribute("managers", managers);
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Thêm Chi Nhánh Mới — UTETra");

        return "admin/branches/form";
    }

    /**
     * Form chỉnh sửa thông tin chi nhánh và cập nhật GPS.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ChiNhanhResponse resp = chiNhanhService.getBranchById(id);

            ChiNhanhRequest branch = ChiNhanhRequest.builder()
                    .id(resp.getId())
                    .maChiNhanh(resp.getMaChiNhanh())
                    .tenChiNhanh(resp.getTenChiNhanh())
                    .diaChi(resp.getDiaChi())
                    .latitude(resp.getLatitude() != null ? resp.getLatitude() : 10.85072)
                    .longitude(resp.getLongitude() != null ? resp.getLongitude() : 106.77196)
                    .hotline(resp.getHotline())
                    .gioMoCua(resp.getGioMoCua())
                    .gioDongCua(resp.getGioDongCua())
                    .active(resp.isActive())
                    .managerId(resp.getManagerId())
                    .build();

            List<UserResponse> managers = chiNhanhService.getEligibleManagers();

            model.addAttribute("branch", branch);
            model.addAttribute("managers", managers);
            model.addAttribute("isEdit", true);
            model.addAttribute("branchId", id);
            model.addAttribute("pageTitle", "Cập Nhật Chi Nhánh: " + resp.getTenChiNhanh() + " — UTETra");

            return "admin/branches/form";
        } catch (Exception e) {
            log.error("Lỗi khi mở form sửa chi nhánh ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/branches";
        }
    }

    /**
     * Lưu thông tin chi nhánh (Tạo mới hoặc Cập nhật).
     */
    @PostMapping("/save")
    public String saveBranch(
            @ModelAttribute ChiNhanhRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (request.getId() == null) {
                ChiNhanhResponse created = chiNhanhService.createBranch(request);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã tạo thành công chi nhánh mới: [" + created.getMaChiNhanh() + "] " + created.getTenChiNhanh());
            } else {
                ChiNhanhResponse updated = chiNhanhService.updateBranch(request.getId(), request);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã cập nhật thông tin chi nhánh: [" + updated.getMaChiNhanh() + "] " + updated.getTenChiNhanh());
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (request.getId() != null) {
                return "redirect:/admin/branches/edit/" + request.getId();
            } else {
                return "redirect:/admin/branches/new";
            }
        } catch (Exception e) {
            log.error("Lỗi khi lưu chi nhánh: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/admin/branches";
        }

        return "redirect:/admin/branches";
    }

    /**
     * Bật / Tắt trạng thái hoạt động nhanh của chi nhánh.
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean newStatus = chiNhanhService.toggleBranchStatus(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã chuyển trạng thái chi nhánh sang: " + (newStatus ? "ĐANG HOẠT ĐỘNG" : "TẠM DỪNG HOẠT ĐỘNG"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/branches";
    }

    /**
     * Xóa chi nhánh (Nếu có nhân sự sẽ tự chuyển ngưng hoạt động).
     */
    @PostMapping("/{id}/delete")
    public String deleteBranch(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            chiNhanhService.deleteBranch(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xử lý xóa / ngưng chi nhánh thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/branches";
    }
}
