package com.ute.trasua.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    private Long id;

    private String name;

    private String description;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private boolean active = true;
}
