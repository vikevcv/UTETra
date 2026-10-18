package com.ute.trasua.controller;

import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.User;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.User;
import com.ute.trasua.service.BranchService;
import com.ute.trasua.service.OnlineOrderService;
import com.ute.trasua.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller for rendering Online Checkout & Success views (Module 3 - UC05).
 */
@Controller
@Slf4j
public class CheckoutViewController {

    private final BranchService branchService;
    private final UserService userService;
    private final OnlineOrderService onlineOrderService;
    private final com.ute.trasua.service.PayOSService payosService;
    private final com.ute.trasua.repository.OrderRepository orderRepository;

    public CheckoutViewController(BranchService branchService,
                                  UserService userService,
                                  OnlineOrderService onlineOrderService,
                                  com.ute.trasua.service.PayOSService payosService,
                                  com.ute.trasua.repository.OrderRepository orderRepository) {
        this.branchService = branchService;
        this.userService = userService;
        this.onlineOrderService = onlineOrderService;
        this.payosService = payosService;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model) {
        List<Branch> activeBranches = branchService.getActiveBranchEntities();
        model.addAttribute("branches", activeBranches);

        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("defaultCustomerName", currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername());
            model.addAttribute("defaultCustomerPhone", currentUser.getPhone() != null ? currentUser.getPhone() : "");
            model.addAttribute("defaultCustomerEmail", currentUser.getEmail() != null ? currentUser.getEmail() : "");
        }

        model.addAttribute("siteTitle", "Thanh Toán & Đặt Hàng Trực Tuyến — UTETra Zen Gallery");
        return "checkout/index";
    }

    /**
     * Màn hình chờ thanh toán trực tuyến VietQR PayOS (UC06).
     */
    @GetMapping("/checkout/payment")
    public String checkoutPaymentPage(@RequestParam("orderCode") String orderCodeParam, Model model) {
        try {
            Order order = resolveOrder(orderCodeParam);
            if (order == null) {
                return "redirect:/cart?error=order_not_found";
            }

            // Nếu đơn đã thanh toán xong từ trước, chuyển ngay sang trang thành công
            if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
                return "redirect:/checkout/success?orderCode=" + order.getOrderCode();
            }

            com.ute.trasua.dto.payment.PayOSCreatePaymentResponse paymentInfo = payosService.createOrGetPaymentLink(order.getOrderCode());

            model.addAttribute("order", order);
            model.addAttribute("paymentInfo", paymentInfo);
            model.addAttribute("siteTitle", "Thanh Toán Đơn Hàng #" + order.getOrderCode() + " — UTETra PayOS");
            return "checkout/payment";
        } catch (Exception e) {
            log.error("Lỗi khi nạp trang thanh toán đơn #{}: {}", orderCodeParam, e.getMessage());
            return "redirect:/cart?error=order_not_found";
        }
    }

    /**
     * Xử lý khi khách hoàn tất thanh toán và được cổng PayOS chuyển hướng về.
     */
    @GetMapping("/checkout/payos/return")
    public String checkoutPayOSReturn(@RequestParam(value = "orderCode", required = false) String orderCodeParam,
                                      @RequestParam(value = "code", required = false) String code,
                                      @RequestParam(value = "status", required = false) String status,
                                      @RequestParam(value = "cancel", required = false) String cancel,
                                      Model model) {
        log.info(">>> PayOS Return URL called: orderCode={}, code={}, status={}, cancel={}",
                orderCodeParam, code, status, cancel);

        if (orderCodeParam == null || orderCodeParam.isBlank()) {
            return "redirect:/";
        }

        Order order = resolveOrder(orderCodeParam);
        if (order == null) {
            log.warn(">>> PayOS Return: Không tìm thấy đơn hàng cho orderCodeParam={}", orderCodeParam);
            return "redirect:/";
        }

        String canonicalOrderCode = order.getOrderCode();
        // Kiểm tra nếu Webhook đã cập nhật PAID hoặc PayOS redirect báo thành công (code=00 hoặc status=PAID)
        boolean isPaid = "PAID".equalsIgnoreCase(order.getPaymentStatus()) ||
                         payosService.checkPaymentStatus(canonicalOrderCode) ||
                         "00".equals(code) ||
                         "PAID".equalsIgnoreCase(status);

        if (isPaid) {
            return "redirect:/checkout/success?orderCode=" + canonicalOrderCode;
        }

        return "redirect:/checkout/payment?orderCode=" + canonicalOrderCode;
    }

    /**
     * Xử lý khi khách bấm hủy giao dịch trên cổng PayOS.
     */
    @GetMapping("/checkout/payos/cancel")
    public String checkoutPayOSCancel(@RequestParam(value = "orderCode", required = false) String orderCodeParam) {
        log.info(">>> Khách hủy thanh toán PayOS cho đơn: {}", orderCodeParam);
        if (orderCodeParam != null && !orderCodeParam.isBlank()) {
            Order order = resolveOrder(orderCodeParam);
            String target = order != null ? order.getOrderCode() : orderCodeParam.trim();
            return "redirect:/checkout/payment?orderCode=" + target + "&cancelled=true";
        }
        return "redirect:/";
    }

    @GetMapping("/checkout/success")
    public String checkoutSuccessPage(@RequestParam("orderCode") String orderCodeParam, Model model) {
        try {
            Order order = resolveOrder(orderCodeParam);
            if (order == null) {
                return "redirect:/cart?error=order_not_found";
            }
            model.addAttribute("order", order);
            model.addAttribute("siteTitle", "Đặt Hàng Thành Công #" + order.getOrderCode() + " — UTETra");
            return "checkout/success";
        } catch (Exception e) {
            log.error("Không tìm thấy đơn hàng #{}: {}", orderCodeParam, e.getMessage());
            return "redirect:/cart?error=order_not_found";
        }
    }

    /**
     * Tra cứu đơn hàng linh hoạt theo cả chuỗi mã hệ thống (ORD-WEB-...) lẫn mã số nguyên PayOS (yyMMdd...).
     * Luôn nạp đầy đủ Chi nhánh và Chi tiết món để render JSP an toàn, không dính LazyInitializationException.
     */
    private Order resolveOrder(String codeParam) {
        if (codeParam == null || codeParam.isBlank()) {
            return null;
        }
        String clean = codeParam.trim();
        String canonicalCode = clean;
        if (clean.matches("^\\d+$")) {
            try {
                Long numCode = Long.parseLong(clean);
                Order byPayos = orderRepository.findByPayosOrderCode(numCode).orElse(null);
                if (byPayos != null) {
                    canonicalCode = byPayos.getOrderCode();
                }
            } catch (Exception ignored) {
            }
        }
        try {
            return onlineOrderService.getOrderByCode(canonicalCode);
        } catch (Exception e) {
            return orderRepository.findWithDetailsByOrderCode(canonicalCode).orElse(null);
        }
    }
}
