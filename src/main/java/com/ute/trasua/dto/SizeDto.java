package com.ute.trasua.dto;

import com.ute.trasua.entity.KichCo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SizeDto {
    private String maKichCo;
    private String tenKichCo;
    private BigDecimal giaPhuThu;
    private String trangThai;
    private boolean hoatDong;

    public static SizeDto fromEntity(KichCo entity) {
        if (entity == null) return null;
        return SizeDto.builder()
                .maKichCo(entity.getMaKichCo())
                .tenKichCo(entity.getTenKichCo())
                .giaPhuThu(entity.getGiaPhuThu())
                .trangThai(entity.getTrangThai())
                .hoatDong(entity.isHoatDong())
                .build();
    }
}
