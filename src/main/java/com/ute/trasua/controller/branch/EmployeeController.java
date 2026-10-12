package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.*;
import com.ute.trasua.service.BranchService;
import com.ute.trasua.service.EmployeeService;
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
 * Controller serving Branch Manager managing employees,
 * work shifts and POS accounts (Module 12 - UC16).
 */
@Controller
@RequestMapping("/branch/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;
    private final BranchService branchService;

    /**
     * Display employees list and shift management + POS accounts (UC16).
     */
    @GetMapping
    public String listEmployees(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication authentication,
            Model model
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long targetBranchId = (currentBranch != null) ? currentBranch.getId() : null;

        Page<EmployeeResponse> employeesPage = employeeService.searchEmployees(
                targetBranchId,
                keyword,
                status,
                PageRequest.of(page, size, Sort.by("id").descending())
        );

        List<PosAccountResponse> posAccounts = List.of();
        if (targetBranchId != null) {
            posAccounts = employeeService.getPosAccountsByBranch(targetBranchId);
        }

        long totalEmp = (targetBranchId != null) ? employeeService.countByBranch(targetBranchId) : 0;
        long activeEmp = (targetBranchId != null) ? employeeService.countActiveByBranch(targetBranchId) : 0;

        List<BranchResponse> allBranches = branchService.getAllBranches();

        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("currentBranchName", currentBranch != null ? currentBranch.getName() : "Toàn Chuỗi");
        model.addAttribute("branchId", targetBranchId);
        model.addAttribute("employeesPage", employeesPage);
        model.addAttribute("posAccounts", posAccounts);
        model.addAttribute("totalEmp", totalEmp);
        model.addAttribute("activeEmp", activeEmp);
        model.addAttribute("allBranches", allBranches);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("pageTitle", "Quản Lý Nhân Sự — UTETra");

        return "branch/employees/list";
    }

    /**
     * Show create new employee form.
     */
    @GetMapping("/new")
    public String showCreateForm(
            @RequestParam(value = "branchId", required = false) Long branchId,
            Authentication authentication,
            Model model
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long defaultBranchId = (currentBranch != null) ? currentBranch.getId() : null;

        EmployeeRequest employee = EmployeeRequest.builder()
                .branchId(defaultBranchId)
                .status("ACTIVE")
                .hireDate(java.time.LocalDate.now())
                .build();

        List<BranchResponse> allBranches = branchService.getAllBranches();

        model.addAttribute("employee", employee);
        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("currentBranchName", currentBranch != null ? currentBranch.getName() : "Toàn Chuỗi");
        model.addAttribute("allBranches", allBranches);
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Tiếp Nhận Nhân Viên Mới — UTETra");

        return "branch/employees/form";
    }

    /**
     * Show edit employee form.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            EmployeeResponse resp = employeeService.getEmployeeById(id);

            EmployeeRequest employee = EmployeeRequest.builder()
                    .id(resp.getId())
                    .employeeCode(resp.getEmployeeCode())
                    .fullName(resp.getFullName())
                    .phone(resp.getPhone())
                    .citizenId(resp.getCitizenId())
                    .email(resp.getEmail())
                    .salary(resp.getSalary())
                    .status(resp.getStatus())
                    .hireDate(resp.getHireDate())
                    .branchId(resp.getBranchId())
                    .build();

            BranchResponse currentBranch = null;
            if (resp.getBranchId() != null) {
                currentBranch = branchService.getBranchById(resp.getBranchId());
            }

            List<BranchResponse> allBranches = branchService.getAllBranches();

            model.addAttribute("employee", employee);
            model.addAttribute("currentBranch", currentBranch);
            model.addAttribute("currentBranchName", currentBranch != null ? currentBranch.getName() : "Toàn Chuỗi");
            model.addAttribute("allBranches", allBranches);
            model.addAttribute("isEdit", true);
            model.addAttribute("employeeId", id);
            model.addAttribute("pageTitle", "Cập Nhật Nhân Viên: " + resp.getFullName() + " — UTETra");

            return "branch/employees/form";
        } catch (Exception e) {
            log.error("Error opening edit form for employee ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/branch/employees";
        }
    }

    /**
     * Save employee info (Create or Update).
     */
    @PostMapping("/save")
    public String saveEmployee(
            @ModelAttribute EmployeeRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (request.getId() == null) {
                EmployeeResponse created = employeeService.createEmployee(request);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã tiếp nhận nhân viên mới: [" + created.getEmployeeCode() + "] " + created.getFullName());
            } else {
                EmployeeResponse updated = employeeService.updateEmployee(request.getId(), request);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã cập nhật hồ sơ nhân viên: [" + updated.getEmployeeCode() + "] " + updated.getFullName());
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (request.getId() != null) {
                return "redirect:/branch/employees/edit/" + request.getId();
            } else {
                return "redirect:/branch/employees/new?branchId=" + request.getBranchId();
            }
        } catch (Exception e) {
            log.error("Error saving employee: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
        }

        return "redirect:/branch/employees" + (request.getBranchId() != null ? "?branchId=" + request.getBranchId() : "");
    }

    /**
     * Update employee work status.
     */
    @PostMapping("/{id}/update-status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) Long branchId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            employeeService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái nhân sự thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/employees" + (branchId != null ? "?branchId=" + branchId : "");
    }

    /**
     * Delete employee profile.
     */
    @PostMapping("/{id}/delete")
    public String deleteEmployee(
            @PathVariable Long id,
            @RequestParam(required = false) Long branchId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            employeeService.deleteEmployee(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa hồ sơ nhân viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/employees" + (branchId != null ? "?branchId=" + branchId : "");
    }

    // ==========================================
    // POS ACCOUNT ACTIONS (UC16)
    // ==========================================

    @PostMapping("/pos/create")
    public String createPosAccount(
            @ModelAttribute PosAccountRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            PosAccountResponse created = employeeService.createPosAccount(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã thiết lập tài khoản quầy POS thành công: " + created.getUsername());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/employees" + (request.getBranchId() != null ? "?branchId=" + request.getBranchId() : "");
    }

    @PostMapping("/pos/{id}/toggle")
    public String togglePosAccount(
            @PathVariable Long id,
            @RequestParam(required = false) Long branchId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean active = employeeService.togglePosAccount(id);
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
            employeeService.deletePosAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài khoản quầy POS!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/employees" + (branchId != null ? "?branchId=" + branchId : "");
    }

    private BranchResponse resolveCurrentBranch(Long requestedBranchId, Authentication authentication) {
        if (requestedBranchId != null) {
            try {
                return branchService.getBranchById(requestedBranchId);
            } catch (Exception ignored) {}
        }

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            var branchOpt = branchService.getBranchByManagerUsername(username);
            if (branchOpt.isPresent()) {
                return branchOpt.get();
            }
        }

        List<BranchResponse> all = branchService.getAllBranches();
        return all.isEmpty() ? null : all.get(0);
    }
}
