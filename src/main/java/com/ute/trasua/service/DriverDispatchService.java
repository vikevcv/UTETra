package com.ute.trasua.service;

import com.ute.trasua.dto.dispatch.DispatchResultDto;

import java.util.Map;

/**
 * Service quản lý điều phối và phân công đơn hàng bắt buộc cho tài xế (Module 9 - UC12).
 */
public interface DriverDispatchService {

    /**
     * Phân công BẮT BUỘC một đơn hàng READY_FOR_DELIVERY cho một tài xế cụ thể.
     */
    DispatchResultDto dispatchMandatoryOrder(Long orderId, Long driverId, String note);

    /**
     * Quét toàn bộ hàng đợi READY_FOR_DELIVERY của chi nhánh và tự động phân công theo 3 tầng ưu tiên.
     * Trả về số đơn đã gán thành công.
     */
    int autoDispatchReadyOrders(Long branchId);

    /**
     * Điều phối lại một đơn hàng vừa được giải phóng do tài xế báo cáo sự cố bất khả kháng.
     */
    DispatchResultDto reassignCancelledOrder(Long orderId);

    /**
     * Lấy snapshot hàng đợi chờ điều phối và trạng thái tài xế cho màn hình Quản lý chi nhánh.
     */
    Map<String, Object> getDispatchQueue(Long branchId);
}
