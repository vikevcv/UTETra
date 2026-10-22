package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.DriverResponse;
import com.ute.trasua.entity.Driver;
import com.ute.trasua.repository.DriverRepository;
import com.ute.trasua.service.DriverService;
import com.ute.trasua.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Controller phục vụ Chủ chuỗi giám sát và quản lý đội ngũ tài xế toàn chuỗi (Module 11 - UC25).
 */
@Controller
@RequestMapping("/admin/drivers")
@RequiredArgsConstructor
@Slf4j
public class DriverManagementController {

    private final DriverRepository driverRepository;
    private final DriverService driverService;
    private final UserManagementService userService;

    /**
     * Màn hình danh sách tài xế, tình trạng trực tuyến, biển số xe và đánh giá (UC25).
     * Dữ liệu 100% đồng bộ thực tế từ Database.
     */
    @GetMapping
    public String listDrivers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        int pageIndex = Math.max(0, page);
        int pageSize = size > 0 ? size : 10;
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Page<Driver> driverEntities = driverRepository.searchAllDrivers(cleanKeyword, pageable);
        Page<DriverResponse> driversPage = driverEntities.map(DriverResponse::fromEntity);

        // Số liệu thống kê đội ngũ tài xế đồng bộ thực tế từ Database
        Map<String, Object> driverStats = driverService.getAllDriverStatistics();

        model.addAttribute("driversPage", driversPage);
        model.addAttribute("stats", driverStats);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageTitle", "Giám Sát Đội Ngũ Giao Hàng — UTETra Admin");

        return "admin/drivers/list";
    }

    /**
     * Khóa hoặc mở khóa quyền nhận đơn của tài xế (UC25).
     */
    @PostMapping("/{id}/toggle-lock")
    public String toggleDriverStatus(
            @PathVariable("id") Long id,
            @RequestParam(value = "locked", required = true) boolean locked,
            @RequestParam(value = "reason", required = false, defaultValue = "Tạm ngưng tiếp nhận đơn") String reason,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Kiểm tra xem id là driverId hay userId
            Driver driver = driverRepository.findById(id).orElse(null);
            if (driver != null) {
                if (driver.getUser() != null) {
                    userService.toggleLock(driver.getUser().getId(), locked, reason);
                }
                driver.setWorkingStatus(locked ? "OFFLINE" : "ONLINE");
                driverRepository.save(driver);
            } else {
                // Xử lý khi id là userId
                userService.toggleLock(id, locked, reason);
                driverRepository.findByUserId(id).ifPresent(d -> {
                    d.setWorkingStatus(locked ? "OFFLINE" : "ONLINE");
                    driverRepository.save(d);
                });
            }

            String message = locked 
                    ? "Đã tạm dừng quyền nhận đơn của tài xế!" 
                    : "Đã kích hoạt lại trạng thái hoạt động cho tài xế!";
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            log.error("Lỗi khi thay đổi trạng thái tài xế {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/drivers";
    }
}
