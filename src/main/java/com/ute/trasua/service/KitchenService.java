package com.ute.trasua.service;

import com.ute.trasua.dto.kitchen.KitchenOrderDto;

import java.util.List;

/**
 * Service xử lý tiếp nhận và điều phối pha chế đồ uống (UC08).
 * 100% tuân thủ tên phương thức Tiếng Anh theo đặc tả Báo cáo SE.
 */
public interface KitchenService {

    /**
     * Lấy danh sách hàng đợi đơn hàng cần pha chế tại chi nhánh.
     * @param branchId ID chi nhánh
     * @return Danh sách đơn hàng trong hàng đợi
     */
    List<KitchenOrderDto> getKitchenOrders(Long branchId);

    /**
     * Tiếp nhận đơn hàng và bắt đầu pha chế (UC08).
     * Chuyển trạng thái đơn sang 'PREPARING'.
     * @param orderId ID đơn hàng
     * @return Thông tin đơn hàng cập nhật
     */
    KitchenOrderDto acceptOrderForPreparation(Long orderId);

    /**
     * Báo hoàn tất pha chế đơn hàng (UC08).
     * Chuyển trạng thái đơn sang 'PREPARED' (Sẵn sàng giao cho shipper hoặc quầy giao khách).
     * @param orderId ID đơn hàng
     * @return Thông tin đơn hàng cập nhật
     */
    KitchenOrderDto completePreparation(Long orderId);

    /**
     * Tra cứu chi tiết đơn hàng KDS theo ID.
     * @param orderId ID đơn hàng
     * @return Thông tin đơn hàng KDS
     */
    KitchenOrderDto getKitchenOrderById(Long orderId);
}
