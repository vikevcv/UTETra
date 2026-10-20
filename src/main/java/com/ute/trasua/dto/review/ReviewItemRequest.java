package com.ute.trasua.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewItemRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Thiếu mã món trong đơn")
    private Long orderDetailId;

    private Long productId;

    @NotNull(message = "Vui lòng chọn số sao")
    @Min(value = 1, message = "Số sao tối thiểu là 1")
    @Max(value = 5, message = "Số sao tối đa là 5")
    private Integer rating;

    private String comment;
}
