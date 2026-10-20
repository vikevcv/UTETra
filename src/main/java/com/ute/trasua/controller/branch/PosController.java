package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.pos.*;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.PosAccount;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.service.BranchService;
import com.ute.trasua.service.PosService;
import com.ute.trasua.service.ReceiptService;
import com.ute.trasua.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
public class PosController {

    private final PosService posService;
    private final ReceiptService receiptService;
    private final BranchService branchService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;

    public PosController(PosService posService,
                         ReceiptService receiptService,
                         BranchService branchService,
                         UserService userService,
                         PasswordEncoder passwordEncoder,
                         OrderRepository orderRepository) {
        this.posService = posService;
        this.receiptService = receiptService;
        this.branchService = branchService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.orderRepository = orderRepository;
    }

    /**
     * Màn hình đăng nhập tài khoản máy POS quầy thu ngân.
     */
    @GetMapping("/pos/login")
    public String posLoginPage(HttpSession session, Model model) {
        if (session.getAttribute("POS_ACCOUNT") != null) {
            return "redirect:/pos";
        }
        return "pos/login";
    }

    /**
     * Xử lý xác thực đăng nhập tài khoản máy POS.
     */
    @PostMapping("/pos/login")
    public String processPosLogin(@RequestParam("username") String username,
                                  @RequestParam("password") String password,
                                  HttpSession session,
                                  Model model) {
        PosAccount posAccount = posService.findPosAccountByUsername(username).orElse(null);
        if (posAccount != null && posAccount.isActive() && passwordEncoder.matches(password, posAccount.getPassword())) {
            session.setAttribute("POS_ACCOUNT", posAccount);
            session.setAttribute("POS_BRANCH_ID", posAccount.getBranch() != null ? posAccount.getBranch().getId() : 1L);
            session.setAttribute("POS_COUNTER_NAME", posAccount.getCounterName());
            log.info(">>> Đăng nhập quầy POS thành công: {} ({})", posAccount.getCounterName(), posAccount.getUsername());
            return "redirect:/pos";
        }

        // Hỗ trợ đăng nhập bằng tài khoản người dùng hệ thống (STAFF / POS / ADMIN / MANAGER)
        User user = userService.findByUsername(username).orElse(null);
        if (user != null && user.isEnabled() && !user.isLocked() && passwordEncoder.matches(password, user.getPassword())) {
            Branch defaultBranch = branchService.getDefaultBranch();
            PosAccount accountToUse = posService.resolvePosAccountForUser(user, defaultBranch);

            session.setAttribute("POS_ACCOUNT", accountToUse);
            session.setAttribute("POS_BRANCH_ID", accountToUse.getBranch() != null ? accountToUse.getBranch().getId() : (defaultBranch != null ? defaultBranch.getId() : 1L));
            session.setAttribute("POS_COUNTER_NAME", accountToUse.getCounterName());
            log.info(">>> Đăng nhập quầy POS thành công qua tài khoản người dùng: {} ({})", accountToUse.getCounterName(), user.getUsername());
            return "redirect:/pos";
        }

        model.addAttribute("error", "Sai tên tài khoản máy POS hoặc mật khẩu! (Gợi ý: thuduc / 123123 hoặc quan1 / 123123)");
        return "pos/login";
    }

    /**
     * Đăng xuất ca trực quầy POS -> Xóa session, cookie và chuyển hướng về /pos/login.
     */
    @GetMapping("/pos/logout")
    public String posLogout(HttpSession session, HttpServletResponse response) {
        if (session != null) {
            session.removeAttribute("POS_ACCOUNT");
            session.removeAttribute("POS_BRANCH_ID");
            session.removeAttribute("POS_COUNTER_NAME");
            session.removeAttribute("POS_ACTIVE_CASHIERS_NAME");
            session.removeAttribute("POS_ACTIVE_CASHIERS_IDS");
            session.invalidate();
        }

        Cookie cookie = new Cookie("UTETRA_JWT", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        SecurityContextHolder.clearContext();
        log.info(">>> Đăng xuất khỏi quầy POS thành công. Chuyển hướng về /pos/login.");
        return "redirect:/pos/login";
    }

    /**
     * Giao diện màn hình Web POS bán lẻ cảm ứng toàn màn hình tại quầy thu ngân (Cố định theo chi nhánh đã đăng nhập).
     */
    @GetMapping({"/pos", "/branch/pos"})
    public String posCounterView(@RequestParam(value = "branchId", required = false) Long branchId,
                                 HttpSession session,
                                 Model model) {
        // Nếu chưa có tài khoản quầy trong session
        if (session.getAttribute("POS_ACCOUNT") == null) {
            // Tự động nhận diện nếu đã đăng nhập tài khoản ADMIN, MANAGER hoặc STAFF/POS qua hệ thống web
            User user = userService.getCurrentAuthenticatedUser();
            if (user != null && user.getRoles() != null && 
                (user.getRoles().contains("ROLE_ADMIN") || user.getRoles().contains("ROLE_MANAGER") || user.getRoles().contains("ROLE_STAFF") || user.getRoles().contains("ROLE_POS"))) {
                Branch defaultBranch = branchService.getDefaultBranch();
                PosAccount accountToUse = posService.resolvePosAccountForUser(user, defaultBranch);
                session.setAttribute("POS_ACCOUNT", accountToUse);
                session.setAttribute("POS_BRANCH_ID", accountToUse.getBranch() != null ? accountToUse.getBranch().getId() : (defaultBranch != null ? defaultBranch.getId() : 1L));
                session.setAttribute("POS_COUNTER_NAME", accountToUse.getCounterName());
            } else {
                // Bắt buộc chuyển về màn hình đăng nhập quầy POS
                return "redirect:/pos/login";
            }
        }

        PosAccount posAcc = (PosAccount) session.getAttribute("POS_ACCOUNT");
        Branch currentBranch = (posAcc != null && posAcc.getBranch() != null) ? posAcc.getBranch() : null;
        if (currentBranch == null) {
            Long sessionBranchId = (Long) session.getAttribute("POS_BRANCH_ID");
            if (sessionBranchId != null) {
                currentBranch = branchService.findBranchEntityById(sessionBranchId).orElse(null);
            }
        }
        if (currentBranch == null) {
            currentBranch = branchService.getDefaultBranch();
        }

        Long activeBranchId = (currentBranch != null) ? currentBranch.getId() : 1L;
        session.setAttribute("POS_BRANCH_ID", activeBranchId);

        String activeCashiers = (String) session.getAttribute("POS_ACTIVE_CASHIERS_NAME");
        if (activeCashiers == null || activeCashiers.isBlank()) {
            if (posAcc != null && posAcc.getCounterName() != null) {
                activeCashiers = posAcc.getCounterName();
            } else {
                activeCashiers = "Chưa có người trực ca";
            }
        }

        model.addAttribute("cashierUsername", activeCashiers);
        model.addAttribute("cashierName", activeCashiers);
        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("branchId", activeBranchId);
        model.addAttribute("branchName", currentBranch != null ? currentBranch.getName() : "UTETra Flagship Thủ Đức");

        return "pos/index";
    }

    /**
     * Giao diện màn hình Bàn Giao Đơn Quầy (Module 8 - UC09, UC18).
     * Màn hình vận hành điểm bán cố định theo chi nhánh quầy.
     */
    @GetMapping({"/pos/handover", "/branch/handover"})
    public String posHandoverView(@RequestParam(value = "branchId", required = false) Long branchId,
                                 HttpSession session,
                                 Model model) {
        Branch currentBranch = null;
        PosAccount posAcc = (PosAccount) session.getAttribute("POS_ACCOUNT");
        if (posAcc != null && posAcc.getBranch() != null) {
            currentBranch = posAcc.getBranch();
        } else {
            Long sessionBranchId = (Long) session.getAttribute("POS_BRANCH_ID");
            if (sessionBranchId != null) {
                currentBranch = branchService.findBranchEntityById(sessionBranchId).orElse(null);
            }
        }
        if (currentBranch == null) {
            currentBranch = branchService.getDefaultBranch();
        }
        Long activeBranchId = (currentBranch != null) ? currentBranch.getId() : 1L;
        session.setAttribute("POS_BRANCH_ID", activeBranchId);

        String activeCashiers = (String) session.getAttribute("POS_ACTIVE_CASHIERS_NAME");
        if (activeCashiers == null || activeCashiers.isBlank()) {
            if (posAcc != null && posAcc.getCounterName() != null) {
                activeCashiers = posAcc.getCounterName();
            } else {
                activeCashiers = "Nhân Viên Bàn Giao";
            }
        }

        model.addAttribute("cashierUsername", activeCashiers);
        model.addAttribute("cashierName", activeCashiers);
        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("branchId", activeBranchId);
        model.addAttribute("branchName", currentBranch != null ? currentBranch.getName() : "UTETra Flagship Thủ Đức");

        return "pos/handover";
    }

    /**
     * API tải danh mục, món trà sữa, size và topping hiển thị trên POS.
     */
    @GetMapping("/api/pos/menu")
    @ResponseBody
    public ResponseEntity<PosMenuResponse> getPosMenu(
            @RequestParam(value = "branchId", required = false) Long branchId) {
        PosMenuResponse menu = posService.getPosMenu(branchId);
        return ResponseEntity.ok(menu);
    }

    /**
     * API lấy danh sách nhân viên của chi nhánh để điểm danh trực ca.
     */
    @GetMapping("/api/pos/employees")
    @ResponseBody
    public ResponseEntity<List<ShiftEmployeeDto>> getBranchEmployees(
            @RequestParam(value = "branchId", required = false) Long branchId) {
        List<ShiftEmployeeDto> list = posService.getBranchEmployees(branchId);
        return ResponseEntity.ok(list);
    }

    /**
     * API kiểm tra trạng thái trực ca hiện tại của máy POS.
     */
    @GetMapping("/api/pos/shift/status")
    @ResponseBody
    public ResponseEntity<ShiftAttendanceResponse> getShiftStatus(
            @RequestParam(value = "branchId", required = false) Long branchId,
            HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Long> employeeIds = (List<Long>) session.getAttribute("POS_ACTIVE_SHIFT_EMPLOYEE_IDS");
        ShiftAttendanceResponse response = posService.getShiftStatus(branchId, employeeIds);
        return ResponseEntity.ok(response);
    }

    /**
     * API xác nhận điểm danh nhân viên vào ca trực quầy POS.
     */
    @PostMapping("/api/pos/shift/checkin")
    @ResponseBody
    public ResponseEntity<ShiftAttendanceResponse> checkinShift(
            @RequestBody ShiftAttendanceRequest request,
            HttpSession session) {
        List<Long> employeeIds = request.getEmployeeIds();
        ShiftAttendanceResponse response = posService.getShiftStatus(request.getBranchId(), employeeIds);
        session.setAttribute("POS_ACTIVE_SHIFT_EMPLOYEE_IDS", employeeIds);
        session.setAttribute("POS_ACTIVE_CASHIERS_NAME", response.getActiveCashiersSummary());
        log.info(">>> Điểm danh nhận ca POS thành công: {} ({} nhân viên)",
                response.getActiveCashiersSummary(), response.getActiveCount());
        return ResponseEntity.ok(response);
    }

    /**
     * API kết thúc ca trực quầy POS (Khóa máy POS về trạng thái OFF).
     */
    @PostMapping("/api/pos/shift/checkout")
    @ResponseBody
    public ResponseEntity<ShiftAttendanceResponse> checkoutShift(HttpSession session) {
        session.removeAttribute("POS_ACTIVE_SHIFT_EMPLOYEE_IDS");
        session.removeAttribute("POS_ACTIVE_CASHIERS_NAME");
        log.info(">>> Đã kết thúc ca trực quầy POS! Máy POS chuyển sang trạng thái TẠM KHÓA.");
        ShiftAttendanceResponse response = ShiftAttendanceResponse.builder()
                .shiftActive(false)
                .activeCount(0)
                .activeCashiersSummary("Chưa có người trực ca")
                .activeEmployees(new ArrayList<>())
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * API tạo đơn hàng bán lẻ tại quầy POS và tự động sinh mã VietQR động.
     */
    @PostMapping("/api/pos/orders")
    @ResponseBody
    public ResponseEntity<PosOrderResponse> createPosOrder(
            @RequestBody PosOrderRequest request,
            HttpSession session) {
        if (request.getCashierUsername() == null || request.getCashierUsername().isBlank() || "pos_td01".equals(request.getCashierUsername())) {
            String activeCashiers = (String) session.getAttribute("POS_ACTIVE_CASHIERS_NAME");
            if (activeCashiers != null && !activeCashiers.isBlank()) {
                request.setCashierUsername(activeCashiers);
            }
        }
        Long sessionBranchId = (Long) session.getAttribute("POS_BRANCH_ID");
        if (sessionBranchId == null) {
            throw new IllegalStateException("POS session expired or no active shift!");
        }
        request.setBranchId(sessionBranchId);
        PosOrderResponse response = posService.createPosOrder(request);
        return ResponseEntity.ok(response);
    }

    /**
     * API xác nhận thanh toán đơn hàng POS (Tiền mặt / VietQR).
     */
    @PostMapping("/api/pos/orders/{orderId}/pay")
    @ResponseBody
    public ResponseEntity<PosOrderResponse> processPosPayment(
            @PathVariable Long orderId,
            @RequestBody PosPaymentRequest request,
            HttpSession session) {
        Long sessionBranchId = (Long) session.getAttribute("POS_BRANCH_ID");
        if (sessionBranchId == null) {
            throw new IllegalStateException("POS session expired or no active shift!");
        }
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || order.getBranch() == null || !sessionBranchId.equals(order.getBranch().getId())) {
            throw new IllegalStateException("Order does not belong to the current POS branch!");
        }
        request.setOrderId(orderId);
        PosOrderResponse response = posService.processPosPayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * API lấy dữ liệu in hóa đơn bán lẻ chuẩn quầy.
     */
    @GetMapping("/api/pos/orders/{orderId}/receipt")
    @ResponseBody
    public ResponseEntity<ReceiptResponse> getReceiptData(@PathVariable Long orderId) {
        ReceiptResponse receipt = receiptService.printReceipt(orderId);
        return ResponseEntity.ok(receipt);
    }

    /**
     * API thanh toán tiền mặt 1 bước (chỉ tạo đơn khi thu ngân xác nhận đã thu đủ tiền).
     */
    @PostMapping("/api/pos/orders/checkout-cash")
    @ResponseBody
    public ResponseEntity<PosOrderResponse> checkoutCashOrder(
            @RequestBody PosCashCheckoutRequest request,
            HttpSession session) {
        if (request.getOrder() != null) {
            if (request.getOrder().getCashierUsername() == null || request.getOrder().getCashierUsername().isBlank() || "pos_td01".equals(request.getOrder().getCashierUsername())) {
                String activeCashiers = (String) session.getAttribute("POS_ACTIVE_CASHIERS_NAME");
                if (activeCashiers != null && !activeCashiers.isBlank()) {
                    request.getOrder().setCashierUsername(activeCashiers);
                }
            }
        }
        Long sessionBranchId = (Long) session.getAttribute("POS_BRANCH_ID");
        if (sessionBranchId == null) {
            throw new IllegalStateException("POS session expired or no active shift!");
        }
        if (request.getOrder() != null) {
            request.getOrder().setBranchId(sessionBranchId);
        }
        PosOrderResponse response = posService.checkoutCashOrder(
                request.getOrder(),
                request.getCashTendered(),
                request.getChangeAmount()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * API hủy đơn hàng treo (PENDING) khi khách hủy giỏ hàng hoặc thay đổi món.
     */
    @PostMapping("/api/pos/orders/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<Void> cancelPendingOrder(@PathVariable Long orderId) {
        posService.cancelPendingOrder(orderId);
        return ResponseEntity.ok().build();
    }

    /**
     * API tra cứu chi tiết đơn hàng POS.
     */
    @GetMapping("/api/pos/orders/{orderId}")
    @ResponseBody
    public ResponseEntity<PosOrderResponse> getOrderById(@PathVariable Long orderId) {
        PosOrderResponse order = posService.getPosOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * API kiểm tra trạng thái thanh toán đơn POS (Polling realtime phục vụ modal VietQR).
     */
    @GetMapping("/api/pos/orders/{orderId}/status")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> checkPosPaymentStatus(
            @PathVariable Long orderId,
            HttpSession session) {
        Long sessionBranchId = (Long) session.getAttribute("POS_BRANCH_ID");
        if (sessionBranchId == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("paid", false, "message", "POS session expired"));
        }
        boolean isPaid = posService.checkPosPaymentStatus(orderId);
        return ResponseEntity.ok(java.util.Map.of("paid", isPaid, "orderId", orderId));
    }

    /**
     * API Dev Skip giả lập thanh toán nhanh cho quầy POS (phục vụ test).
     */
    @PostMapping("/api/pos/orders/{orderId}/dev-skip")
    @ResponseBody
    public ResponseEntity<PosOrderResponse> skipPosPaymentForTesting(
            @PathVariable Long orderId,
            HttpSession session) {
        Long sessionBranchId = (Long) session.getAttribute("POS_BRANCH_ID");
        if (sessionBranchId == null) {
            throw new IllegalStateException("POS session expired!");
        }
        PosOrderResponse response = posService.skipPosPaymentForTesting(orderId);
        return ResponseEntity.ok(response);
    }
}
