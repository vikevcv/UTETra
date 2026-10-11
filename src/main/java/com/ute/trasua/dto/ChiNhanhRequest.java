package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiNhanhRequest {

    private Long id;

    private String maChiNhanh;

    private String tenChiNhanh;

    private String diaChi;

    private Double latitude;

    private Double longitude;

    private String hotline;

    @Builder.Default
    private String gioMoCua = "07:00";

    @Builder.Default
    private String gioDongCua = "22:30";

    @Builder.Default
    private boolean active = true;

    private Long managerId;
}
