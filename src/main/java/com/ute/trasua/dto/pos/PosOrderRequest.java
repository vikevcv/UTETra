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
public class PosOrderRequest {

    private Long branchId;
    private String cashierUsername;
    private String note;
    
    @Builder.Default
    private List<PosOrderItemRequest> items = new ArrayList<>();
}
