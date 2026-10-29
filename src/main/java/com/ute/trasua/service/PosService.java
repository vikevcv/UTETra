package com.ute.trasua.service;

import com.ute.trasua.dto.pos.PosMenuResponse;
import com.ute.trasua.dto.pos.PosOrderRequest;
import com.ute.trasua.dto.pos.PosOrderResponse;
import com.ute.trasua.dto.pos.PosPaymentRequest;

public interface PosService {

    /**
     * Lấy toàn bộ danh mục, món trà sữa, size và topping phục vụ màn hình POS.
     */
    PosMenuResponse getPosMenu(Long branchId);

    /**
     * Tạo đơn hàng bán lẻ tại quầy POS, tính tổng tiền và sinh mã VietQR động.
     */
    PosOrderResponse createPosOrder(PosOrderRequest request);

    /**
     * Xử lý xác nhận thanh toán đơn POS (Tiền mặt hoặc Chuyển khoản VietQR).
     */
    PosOrderResponse processPosPayment(PosPaymentRequest request);

    /**
     * Thanh toán Tiền Mặt trực tiếp: Tạo đơn và xác nhận thanh toán hoàn tất trong 1 bước duy nhất.
     */
    PosOrderResponse checkoutCashOrder(PosOrderRequest request, java.math.BigDecimal cashTendered, java.math.BigDecimal changeAmount);

    /**
     * Hủy đơn hàng PENDING khi khách hủy giỏ hàng hoặc đổi món.
     */
    void cancelPendingOrder(Long orderId);

    /**
     * Lấy thông tin chi tiết đơn hàng POS.
     */
    PosOrderResponse getPosOrderById(Long orderId);

    /**
     * Lấy danh sách nhân viên đang hoạt động của chi nhánh phục vụ trực ca.
     */
    java.util.List<com.ute.trasua.dto.pos.ShiftEmployeeDto> getBranchEmployees(Long branchId);

    /**
     * Tính toán và trả về trạng thái trực ca của chi nhánh dựa trên danh sách nhân viên đang tick.
     */
    com.ute.trasua.dto.pos.ShiftAttendanceResponse getShiftStatus(Long branchId, java.util.List<Long> activeEmployeeIds);

    /**
     * Điểm danh nhận ca trực POS và lưu trạng thái ca trực theo chi nhánh.
     */
    com.ute.trasua.dto.pos.ShiftAttendanceResponse checkinShift(Long branchId, java.util.List<Long> employeeIds);

    /**
     * Kết thúc ca trực POS của chi nhánh.
     */
    com.ute.trasua.dto.pos.ShiftAttendanceResponse checkoutShift(Long branchId);

    /**
     * Tìm kiếm tài khoản POS theo tên tài khoản.
     */
    java.util.Optional<com.ute.trasua.entity.PosAccount> findPosAccountByUsername(String username);

    /**
     * Xác định hoặc khởi tạo PosAccount tương ứng cho User đăng nhập.
     */
    com.ute.trasua.entity.PosAccount resolvePosAccountForUser(com.ute.trasua.entity.User user, com.ute.trasua.entity.Branch defaultBranch);

    /**
     * Lấy danh sách tài khoản POS đang kích hoạt theo chi nhánh.
     */
    java.util.List<com.ute.trasua.entity.PosAccount> getActivePosAccountsByBranch(Long branchId);

    /**
     * Kiểm tra trạng thái thanh toán của đơn hàng POS.
     */
    boolean checkPosPaymentStatus(Long orderId);

    /**
     * Giả lập thanh toán thành công cho đơn POS phục vụ kiểm thử.
     */
    PosOrderResponse skipPosPaymentForTesting(Long orderId);
}
