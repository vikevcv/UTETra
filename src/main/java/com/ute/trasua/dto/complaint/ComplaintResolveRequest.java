package com.ute.trasua.dto.complaint;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResolveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * APPROVE (Duyệt bồi thường voucher) hoặc REJECT (Từ chối)
     */
    @NotBlank(message = "Thiếu quyết định xử lý khiếu nại")
    private String action;

    private String adminResponse;

    private BigDecimal compensationRate; // e.g. 0.80
}
