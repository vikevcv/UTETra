package com.ute.trasua.controller;

import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller điều hành giao diện và tác vụ Giao vận di động (Shipper Web App).
 * Phục vụ các Use Case:
 * - UC12: Tiếp nhận phân công đơn hàng bắt buộc từ chi nhánh (Mandatory Dispatch)
 * - UC13: Cập nhật tiến trình giao hàng (Lấy trà -> Check-in Cloudinary bàn giao)
 * - UC14: Xử lý giao thất bại hoàn trả quán / Báo cáo sự cố từ chối ca giao
 * - UC15: Thống kê bảng công ca trực, Lương cứng & Thưởng hiệu suất KPI
 */
@Controller
@RequestMapping("/shipper")
@RequiredArgsConstructor
@Slf4j
public class ShipperController {

    private final UserRepository userRepository;

    /**
     * Màn hình chính bản đồ điều hướng của Tài xế giao hàng.
     */
    @GetMapping({"", "/", "/dashboard"})
    public String shipperDashboard(Authentication authentication, Model model) {
        String username = (authentication != null) ? authentication.getName() : "driver1";
        
        User driverUser = userRepository.findByUsername(username)
                .orElse(null);

        model.addAttribute("username", username);
        model.addAttribute("fullName", driverUser != null ? driverUser.getFullName() : "Nguyễn Ngọc Thoại");
        model.addAttribute("phone", driverUser != null ? driverUser.getPhone() : "0987 654 321");
        model.addAttribute("email", driverUser != null ? driverUser.getEmail() : "driver1@utetra.vn");
        model.addAttribute("driverCode", "TX-018");
        model.addAttribute("vehiclePlate", "59X3-888.99");
        model.addAttribute("branchName", "UTETra Cơ Sở 01 — 01 Võ Văn Ngân, Thủ Đức");
        model.addAttribute("shiftName", "Ca Chiều (13:00 - 21:00)");
        model.addAttribute("baseSalary", "240.000₫");
        model.addAttribute("kpiBonus", "45.000₫");
        model.addAttribute("totalEstimatedEarnings", "285.000₫");
        model.addAttribute("isOnline", false);

        return "shipper/index";
    }

    /**
     * API cập nhật trạng thái BẬT / TẮT ca trực của tài xế.
     */
    @PostMapping("/api/toggle-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleStatus(@RequestBody Map<String, Boolean> body) {
        boolean online = body.getOrDefault("online", false);
        log.info("Shipper cập nhật trạng thái hoạt động: {}", online ? "BẬT (Online)" : "TẮT (Offline)");
        return ResponseEntity.ok(Map.of(
                "success", true,
                "online", online,
                "message", online ? "Đã bật ca trực, sẵn sàng tiếp nhận phân công đơn" : "Đã tắt ca trực, tạm ngưng nhận đơn"
        ));
    }

    /**
     * API báo cáo sự cố bất khả kháng để hoàn trả đơn hàng về cho quán điều phối lại (UC12 / UC14).
     */
    @PostMapping("/api/report-incident")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reportIncident(@RequestBody Map<String, String> body) {
        String orderCode = body.getOrDefault("orderCode", "UT-90255");
        String reasonCode = body.getOrDefault("reasonCode", "xe_hong");
        String note = body.getOrDefault("note", "");

        log.warn("Shipper báo cáo sự cố cho đơn {}: lý do = {}, ghi chú = {}", orderCode, reasonCode, note);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "orderCode", orderCode,
                "message", "Đã ghi nhận sự cố và hoàn trả đơn về chi nhánh để quản lý phân công lại cho tài xế khác."
        ));
    }
}
