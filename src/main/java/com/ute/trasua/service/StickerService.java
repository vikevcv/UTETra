package com.ute.trasua.service;

import com.ute.trasua.dto.kitchen.CupStickerDto;

import java.util.List;

/**
 * Service xử lý sinh và in tem dán ly theo biểu mẫu QLPC_BM1 (UC09).
 * 100% tuân thủ tên phương thức Tiếng Anh theo đặc tả Báo cáo SE.
 */
public interface StickerService {

    /**
     * Sinh toàn bộ danh sách tem dán ly cho tất cả các ly trong đơn hàng (QLPC_BM1).
     * @param orderId ID đơn hàng
     * @return Danh sách tem dán ly
     */
    List<CupStickerDto> printCupStickers(Long orderId);

    /**
     * Sinh lại đúng một tem dán ly cụ thể khi máy in bị kẹt giấy hoặc nhòe mực.
     * @param orderId ID đơn hàng
     * @param cupIndex Thứ tự ly trong đơn (1-based index)
     * @return Tem dán ly được sinh lại
     */
    CupStickerDto printSingleCupSticker(Long orderId, Integer cupIndex);
}
