package com.ute.trasua.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Biểu mẫu nghiệp vụ tham chiếu: QLPC_BM1: PHIẾU PHA CHẾ & TEM DÁN CỐC.
 * In trực tiếp lên sticker nhiệt dán cốc trà sữa (chuẩn 50x30mm hoặc 60x40mm).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CupStickerDto {
    private String orderCode;
    private Integer cupIndex;
    private Integer totalCups;
    private String branchName;
    private String productName;
    private String sizeName;
    private String sugarLevel;
    private String iceLevel;
    private List<String> toppings;
    private String toppingsSummary;
    private String customerNote;
    private Integer dailyQueueNumber;
    private String printedAt;
    @Builder.Default
    private Boolean isReprint = false;
}
