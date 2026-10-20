package com.ute.trasua.service;

import com.ute.trasua.entity.Driver;
import com.ute.trasua.entity.Order;

/**
 * Service interface for publishing real-time WebSocket events (Module 3).
 * Broadcasts status updates to Customer, Kitchen, Handover, and Driver clients.
 */
public interface OrderWebSocketPublisher {

    /**
     * Broadcasts order creation to Customer (/topic/order/{orderCode}) and Kitchen (/topic/kitchen/{branchId}).
     */
    void broadcastOrderCreated(Order order);

    /**
     * Broadcasts preparation start to Customer (/topic/order/{orderCode}).
     */
    void broadcastOrderPreparing(Order order);

    /**
     * Broadcasts preparation completed to Customer and Handover (/topic/branch/{branchId}/handover).
     */
    void broadcastOrderPrepared(Order order);

    /**
     * Broadcasts driver assignment to Customer and Driver (/topic/shipper/{driverId}).
     */
    void broadcastOrderDispatched(Order order, Driver driver);

    /**
     * Broadcasts out-for-delivery status to Customer.
     */
    void broadcastOrderDelivering(Order order, Driver driver);

    /**
     * Broadcasts arrived-at-destination status to Customer, Driver, and Handover.
     */
    void broadcastOrderArrived(Order order, Driver driver);

    /**
     * Broadcasts delivery completed to Customer and Branch.
     */
    void broadcastOrderCompleted(Order order);

    /**
     * Broadcasts order cancellation to Customer, Kitchen, and Shipper.
     */
    void broadcastOrderCancelled(Order order, String reason);

    /**
     * Broadcasts driver force majeure incident to Store Manager, Shipper, and Customer (Module 9 - UC12).
     */
    void broadcastDriverIncident(Order order, Driver driver, String reason, String note);

    /**
     * Broadcasts alert when ready orders remain unassigned due to unavailable drivers.
     */
    void broadcastQueueUnassigned(Long branchId, int unassignedCount, java.util.List<String> orderCodes);

    /**
     * Kích hoạt sự kiện WebSocket STOMP khi đơn hàng trực tuyến thanh toán thành công (UC06):
     * - Báo quầy POS (/topic/pos/{branchId})
     * - Báo Bếp KDS (/topic/kitchen/{branchId})
     * - Báo trình duyệt khách hàng (/topic/order/{orderCode})
     */
    void broadcastOnlineOrderPaid(Order order);
}
