package com.ute.trasua.dto.complaint;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintSubmitRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Vui lòng chọn loại khiếu nại")
    private String complaintType;

    @NotBlank(message = "Vui lòng nhập mô tả sự cố")
    private String description;
}
