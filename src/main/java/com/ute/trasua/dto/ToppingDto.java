package com.ute.trasua.dto;

import com.ute.trasua.entity.Topping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToppingDto {
    private Long maTopping;
    private String tenTopping;
    private BigDecimal donGia;
    private String trangThai;
    private boolean hoatDong;

    public static ToppingDto fromEntity(Topping entity) {
        if (entity == null) return null;
        return ToppingDto.builder()
                .maTopping(entity.getMaTopping())
                .tenTopping(entity.getTenTopping())
                .donGia(entity.getDonGia())
                .trangThai(entity.getTrangThai())
                .hoatDong(entity.isHoatDong())
                .build();
    }
}
