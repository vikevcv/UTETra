package com.ute.trasua.controller;

import com.ute.trasua.dto.order.OrderTrackingDto;
import com.ute.trasua.service.OrderTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller for rendering the Customer Live Order Tracking view (Module 3).
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderTrackingViewController {

    private final OrderTrackingService orderTrackingService;

    @GetMapping("/order/tracking")
    public String orderTrackingPage(
            @RequestParam(value = "code", required = false) String code,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()))
                ? auth.getName()
                : null;

        if (code != null && !code.trim().isEmpty()) {
            try {
                OrderTrackingDto tracking = orderTrackingService.getTrackingDetails(code.trim());
                model.addAttribute("tracking", tracking);
                model.addAttribute("orderCode", tracking.getOrderCode());
                model.addAttribute("siteTitle", "Theo Dõi Đơn Hàng #" + tracking.getOrderCode() + " — UTETra Zen Gallery");
            } catch (Exception e) {
                log.warn("Không tìm thấy đơn hàng khi tra cứu code {}: {}", code, e.getMessage());
                model.addAttribute("searchError", "Không tìm thấy thông tin cho mã đơn: " + code);
                model.addAttribute("siteTitle", "Tra Cứu Tiến Trình Đơn Hàng — UTETra Zen Gallery");
            }
        } else {
            model.addAttribute("siteTitle", "Tra Cứu Tiến Trình Đơn Hàng — UTETra Zen Gallery");
        }

        // Tải danh sách đơn hàng đã đặt của người dùng hiện tại
        if (username != null) {
            List<OrderTrackingDto> recentOrders = orderTrackingService.getRecentOrdersForUsername(username);
            model.addAttribute("recentOrders", recentOrders);
            model.addAttribute("currentUsername", username);
        }

        return "order/tracking";
    }

    @GetMapping("/order/tracking/{orderCode}")
    public String orderTrackingByPath(
            @PathVariable("orderCode") String orderCode,
            Model model) {
        return orderTrackingPage(orderCode, model);
    }
}
