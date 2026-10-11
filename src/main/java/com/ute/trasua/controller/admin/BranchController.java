package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.BranchRequest;
import com.ute.trasua.dto.BranchResponse;
import com.ute.trasua.dto.UserResponse;
import com.ute.trasua.service.BranchService;
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
 * Controller serving Chain Admin managing Branch network (Module 12 - UC21).
 */
@Controller
@RequestMapping("/admin/branches")
@RequiredArgsConstructor
@Slf4j
public class BranchController {

    private final BranchService branchService;

    /**
     * Display all branches network list and map overview (UC21).
     */
    @GetMapping
    public String listBranches(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            Model model
    ) {
        Page<BranchResponse> branchesPage = branchService.searchBranches(
                keyword,
                active,
                PageRequest.of(page, size, Sort.by("id").descending())
        );

        long totalBranches = branchService.countTotalBranches();
        long activeBranches = branchService.countActiveBranches();
        long pausedBranches = totalBranches - activeBranches;

        List<BranchResponse> allBranches = branchService.getAllBranches();

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
     * Show create new branch form with interactive map picker.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        BranchRequest branch = BranchRequest.builder()
                .openTime("07:00")
                .closeTime("22:30")
                .active(true)
                .latitude(10.85072) // Default coordinates: HCMUTE Thu Duc
                .longitude(106.77196)
                .build();

        List<UserResponse> managers = branchService.getEligibleManagers();

        model.addAttribute("branch", branch);
        model.addAttribute("managers", managers);
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Thêm Chi Nhánh Mới — UTETra");

        return "admin/branches/form";
    }

    /**
     * Show edit branch form and GPS coordinates.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            BranchResponse resp = branchService.getBranchById(id);

            BranchRequest branch = BranchRequest.builder()
                    .id(resp.getId())
                    .branchCode(resp.getBranchCode())
                    .name(resp.getName())
                    .address(resp.getAddress())
                    .latitude(resp.getLatitude() != null ? resp.getLatitude() : 10.85072)
                    .longitude(resp.getLongitude() != null ? resp.getLongitude() : 106.77196)
                    .hotline(resp.getHotline())
                    .openTime(resp.getOpenTime())
                    .closeTime(resp.getCloseTime())
                    .active(resp.isActive())
                    .managerId(resp.getManagerId())
                    .build();

            List<UserResponse> managers = branchService.getEligibleManagers();

            model.addAttribute("branch", branch);
            model.addAttribute("managers", managers);
            model.addAttribute("isEdit", true);
            model.addAttribute("branchId", id);
            model.addAttribute("pageTitle", "Cập Nhật Chi Nhánh: " + resp.getName() + " — UTETra");

            return "admin/branches/form";
        } catch (Exception e) {
            log.error("Error opening edit form for branch ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/branches";
        }
    }

    /**
     * Save branch information (Create or Update).
     */
    @PostMapping("/save")
    public String saveBranch(
            @ModelAttribute BranchRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (request.getId() == null) {
                BranchResponse created = branchService.createBranch(request);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã tạo thành công chi nhánh mới: [" + created.getBranchCode() + "] " + created.getName());
            } else {
                BranchResponse updated = branchService.updateBranch(request.getId(), request);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã cập nhật thông tin chi nhánh: [" + updated.getBranchCode() + "] " + updated.getName());
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (request.getId() != null) {
                return "redirect:/admin/branches/edit/" + request.getId();
            } else {
                return "redirect:/admin/branches/new";
            }
        } catch (Exception e) {
            log.error("Error saving branch: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/admin/branches";
        }

        return "redirect:/admin/branches";
    }

    /**
     * Fast toggle branch active/pause status.
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean newStatus = branchService.toggleBranchStatus(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã chuyển trạng thái chi nhánh sang: " + (newStatus ? "ĐANG HOẠT ĐỘNG" : "TẠM DỪNG HOẠT ĐỘNG"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/branches";
    }

    /**
     * Delete / deactivate branch.
     */
    @PostMapping("/{id}/delete")
    public String deleteBranch(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            branchService.deleteBranch(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xử lý xóa / ngưng chi nhánh thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/branches";
    }
}
