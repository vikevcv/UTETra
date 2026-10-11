package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.DriverResponse;
import com.ute.trasua.dto.UserResponse;
import com.ute.trasua.dto.UserSearchRequest;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * Controller trang chủ Bảng điều khiển (Executive Dashboard) dành riêng cho Chủ Chuỗi.
 * Tổng hợp chỉ số KPI toàn hệ thống, truy cập nhanh các phân hệ M11-M15.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardController {

    private final UserManagementService userService;
    private final UserRepository userRepository;

    /**
     * Điều hướng trang chủ Dashboard Chủ Chuỗi.
     */
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        // 1. Thống kê số lượng người dùng từ DB
        Map<String, Object> userStats = userService.getUserStatistics();

        // 2. Thống kê đội ngũ tài xế
        List<User> drivers = userRepository.findAll((root, query, cb) -> cb.isMember("ROLE_DRIVER", root.get("roles")));
        long totalDrivers = drivers.size();
        long activeDrivers = drivers.stream().filter(d -> !d.isLocked()).count();
        long onlineDrivers = Math.max(0, (long) Math.ceil(activeDrivers * 0.65));

        // 3. Lấy top 5 người dùng đăng ký gần đây nhất
        Page<UserResponse> recentUsersPage = userService.searchUsers(UserSearchRequest.builder()
                .page(0)
                .size(5)
                .sortBy("id")
                .sortDir("desc")
                .build());

        // 4. Lấy top 3 tài xế đang trực tuyến
        Page<User> recentDriversPage = userRepository.searchDrivers(null, PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "id")));
        List<DriverResponse> topDrivers = recentDriversPage.getContent().stream()
                .map(DriverResponse::fromUser)
                .toList();

        model.addAttribute("userStats", userStats);
        model.addAttribute("totalDrivers", totalDrivers);
        model.addAttribute("onlineDrivers", onlineDrivers);
        model.addAttribute("recentUsers", recentUsersPage.getContent());
        model.addAttribute("topDrivers", topDrivers);
        model.addAttribute("pageTitle", "Bảng Điều Khiển Chủ Chuỗi — UTETra Executive Portal");

        return "admin/dashboard";
    }
}
