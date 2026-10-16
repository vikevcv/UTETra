package com.ute.trasua.service;

import com.ute.trasua.dto.order.OrderTrackingDto;

import java.util.List;

/**
 * Service for customer order tracking and status inspection (Module 3).
 */
public interface OrderTrackingService {

    /**
     * Gets complete tracking details for an order by its code.
     *
     * @param orderCode Unique order code
     * @return OrderTrackingDto populated with branch, items, and driver info
     */
    OrderTrackingDto getTrackingDetails(String orderCode);

    /**
     * Look up recent orders by customer phone number.
     *
     * @param phoneNumber Vietnamese customer phone number
     * @return List of recent OrderTrackingDto
     */
    List<OrderTrackingDto> lookupOrdersByPhone(String phoneNumber);

    /**
     * Look up recent orders for the currently authenticated user.
     *
     * @param username Username of the logged-in customer
     * @return List of recent OrderTrackingDto
     */
    List<OrderTrackingDto> getRecentOrdersForUsername(String username);
}
