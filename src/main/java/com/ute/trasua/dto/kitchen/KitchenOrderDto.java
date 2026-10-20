package com.ute.trasua.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenOrderDto {
    private Long orderId;
    private String orderCode;
    private Integer dailyQueueNumber;
    private String orderType; // POS_WALK_IN, ONLINE_DELIVERY, ONLINE_PICKUP
    private String status; // PAID, PREPARING, PREPARED, READY_FOR_PICKUP, READY_FOR_DELIVERY
    private String statusLabel; // Đang Pha Chế, Chờ Bàn Giao, ...
    private String cashierOrCustomerName;
    private String createdAtFormatted;
    private Long elapsedMinutes;
    private Integer totalCups;
    private String note;
    @Builder.Default
    private List<KitchenOrderItemDto> items = new ArrayList<>();
}
