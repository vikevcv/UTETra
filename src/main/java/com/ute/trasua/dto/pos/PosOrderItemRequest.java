package com.ute.trasua.dto.pos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosOrderItemRequest {

    private Long productId;
    private Long sizeId;
    
    @Builder.Default
    private String sugarLevel = "100%";
    
    @Builder.Default
    private String iceLevel = "100%";
    
    @Builder.Default
    private List<Long> toppingIds = new ArrayList<>();
    
    @Builder.Default
    private Integer quantity = 1;
    
    private String note;
}
