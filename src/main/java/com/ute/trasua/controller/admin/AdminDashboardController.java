package com.ute.trasua.controller.admin;

import com.ute.trasua.dto.DriverResponse;
import com.ute.trasua.dto.RevenueReportRequest;
import com.ute.trasua.dto.RevenueReportResponse;
import com.ute.trasua.dto.UserResponse;
import com.ute.trasua.dto.UserSearchRequest;
import com.ute.trasua.entity.Driver;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.CategoryRepository;
import com.ute.trasua.repository.DriverRepository;
import com.ute.trasua.repository.IngredientRepository;
import com.ute.trasua.repository.ProductRepository;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.RevenueReportService;
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
 * Tổng hợp chỉ số KPI thực tế từ Database toàn hệ thống, truy cập nhanh các phân hệ M11-M15.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardController {

    private final UserManagementService userService;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final RevenueReportService revenueReportService;

    /**
     * Điều hướng trang chủ Dashboard Chủ Chuỗi với số liệu 100% đồng bộ thực tế từ Database.
     */
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        // 1. Thống kê số lượng người dùng từ DB
        Map<String, Object> userStats = userService.getUserStatistics();

        // 2. Thống kê đội ngũ tài xế đồng bộ thực tế từ Database
        long totalDrivers = driverRepository.countTotalDrivers();
        long onlineDrivers = driverRepository.countActiveOnlineDrivers();
        int onlineDriverPercentage = totalDrivers > 0 ? (int) Math.round((onlineDrivers * 100.0) / totalDrivers) : 0;

        // 3. Thống kê mạng lưới chi nhánh thực tế
        long totalBranches = branchRepository.count();
        long activeBranches = branchRepository.findByActiveTrue().size();

        // 4. Thống kê doanh thu và đơn hàng hôm nay thực tế từ Database
        RevenueReportRequest todayRequest = RevenueReportRequest.builder()
                .period("TODAY")
                .build();
        RevenueReportResponse todayReport = revenueReportService.generateRevenueReport(todayRequest);

        // 5. Thống kê Thực đơn & Kho hàng thực tế
        long totalProducts = productRepository.count();
        long totalCategories = categoryRepository.count();
        long totalIngredients = ingredientRepository.count();

        // 6. Lấy top 5 người dùng đăng ký gần đây nhất
        Page<UserResponse> recentUsersPage = userService.searchUsers(UserSearchRequest.builder()
                .page(0)
                .size(5)
                .sortBy("id")
                .sortDir("desc")
                .build());

        // 7. Lấy top 3 tài xế hoạt động thực tế từ Database
        List<Driver> activeDriversList = driverRepository.findTopActiveDrivers(PageRequest.of(0, 3));
        List<DriverResponse> topDrivers = activeDriversList.stream()
                .map(DriverResponse::fromEntity)
                .toList();

        model.addAttribute("userStats", userStats);
        model.addAttribute("totalDrivers", totalDrivers);
        model.addAttribute("onlineDrivers", onlineDrivers);
        model.addAttribute("onlineDriverPercentage", onlineDriverPercentage);
        model.addAttribute("totalBranches", totalBranches);
        model.addAttribute("activeBranches", activeBranches);
        model.addAttribute("todayReport", todayReport);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalIngredients", totalIngredients);
        model.addAttribute("recentUsers", recentUsersPage.getContent());
        model.addAttribute("topDrivers", topDrivers);
        model.addAttribute("pageTitle", "Bảng Điều Khiển Chủ Chuỗi — UTETra Executive Portal");

        return "admin/dashboard";
    }
}
