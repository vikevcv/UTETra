package com.ute.trasua.service;

import com.ute.trasua.dto.pos.ReceiptResponse;

public interface ReceiptService {

    /**
     * Lấy và đóng gói toàn bộ dữ liệu phiếu in hóa đơn bán lẻ chuẩn quầy chi nhánh.
     */
    ReceiptResponse printReceipt(Long orderId);
}
