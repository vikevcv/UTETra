package com.ute.trasua.dto.review;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSubmitRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Danh sách đánh giá không được để trống")
    @Valid
    @JsonAlias({"items", "reviews"})
    @Builder.Default
    private List<ReviewItemRequest> reviews = new ArrayList<>();

    private String customerName;
}
