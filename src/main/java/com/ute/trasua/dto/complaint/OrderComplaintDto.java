package com.ute.trasua.dto.complaint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderComplaintDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private String orderCode;
    private String customerName;
    private String customerPhone;
    private String branchName;
    private String complaintType;
    private String complaintTypeLabel;
    private String description;
    private String status;
    private String statusLabel;
    private String compensationVoucherCode;
    private BigDecimal compensationAmount;
    private String adminResponse;
    private String resolvedByUsername;
    private LocalDateTime createdAt;
    private String formattedCreatedAt;
    private LocalDateTime resolvedAt;
    private String formattedResolvedAt;
}
