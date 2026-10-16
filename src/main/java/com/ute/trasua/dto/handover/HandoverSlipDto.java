package com.ute.trasua.dto.handover;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO Biểu mẫu bàn giao giao hàng QLGH_BM1.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandoverSlipDto {

    @Builder.Default
    private String slipCode = "QLGH_BM1";

    private Long orderId;
    private String orderCode;
    private String branchName;
    private String branchAddress;
    private String branchPhone;
    private String driverName;
    private String driverPhone;
    private String vehiclePlate;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;

    @Builder.Default
    private List<String> itemsList = new ArrayList<>();

    private Integer totalCups;
    private LocalDateTime handedOverAt;
    private String handedOverBy;
}
