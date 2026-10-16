package com.ute.trasua.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing an individual item inside an order on the tracking screen.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemTrackingDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;

    private String sizeName;
    private String sugarLevel;
    private String iceLevel;

    @Builder.Default
    private List<String> toppings = new ArrayList<>();

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String note;
}
