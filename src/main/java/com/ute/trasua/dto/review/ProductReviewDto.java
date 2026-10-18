package com.ute.trasua.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private Long orderDetailId;
    private Long productId;
    private String productName;
    private String customerName;
    private Integer rating;
    private String comment;
    private Boolean isHidden;
    private LocalDateTime createdAt;
    private String formattedCreatedAt;
}
