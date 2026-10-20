package com.ute.trasua.service;

import com.ute.trasua.dto.handover.HandoverActionRequest;
import com.ute.trasua.dto.handover.HandoverOrderDto;
import com.ute.trasua.dto.handover.HandoverSlipDto;

import java.util.List;

/**
 * Service quản lý bàn giao đơn hàng (Module 8 - Bàn giao, Điều phối & Quản lý giao hàng).
 */
public interface OrderHandoverService {

    /**
     * Lấy danh sách các đơn hàng sẵn sàng chờ bàn giao (cho tài xế hoặc khách hàng).
     */
    List<HandoverOrderDto> getPendingHandovers(Long branchId);

    /**
     * Bàn giao đơn hàng giao tận nơi cho tài xế giao hàng.
     */
    HandoverOrderDto handoverToDriver(Long orderId, Long driverId, String notes);

    /**
     * Bàn giao đơn hàng lấy tại quầy cho khách hàng.
     */
    HandoverOrderDto handoverToCustomer(Long orderId);

    /**
     * Lấy dữ liệu in phiếu bàn giao giao hàng theo biểu mẫu QLGH_BM1.
     */
    HandoverSlipDto getHandoverSlipData(Long orderId);

    /**
     * Lấy danh sách đơn hàng được gán cho tài xế theo trạng thái.
     */
    List<HandoverOrderDto> getDriverOrders(Long driverId, List<String> statuses);

    /**
     * Cập nhật tiến độ giao hàng của tài xế (hoàn tất hoặc báo cáo sự cố).
     */
    HandoverOrderDto updateDeliveryStatus(Long orderId, Long driverId, String status, String note);

    /**
     * Tài xế xác nhận nhận hàng tại quầy kèm ảnh ly nước đối soát (UC13 - Bước 1).
     */
    HandoverOrderDto confirmDriverPickedUp(Long orderId, Long driverId, com.ute.trasua.dto.handover.DeliveryActionRequest request);

    /**
     * Tài xế xác nhận đã tới điểm giao của khách (UC13 - Bước 2/3).
     */
    HandoverOrderDto confirmArrivedAtCustomer(Long orderId, Long driverId);

    /**
     * Tài xế xác nhận đã giao hàng thành công kèm ảnh trao nước cho khách (UC13 - Bước 4).
     */
    HandoverOrderDto confirmDeliverySuccess(Long orderId, Long driverId, com.ute.trasua.dto.handover.DeliveryActionRequest request);

    /**
     * Tài xế báo cáo giao hàng thất bại kèm ảnh bằng chứng và tự động hủy đơn hoàn trả (UC14 Tự Động).
     */
    HandoverOrderDto reportDeliveryFailure(Long orderId, Long driverId, com.ute.trasua.dto.handover.DeliveryFailureRequest request);
}
