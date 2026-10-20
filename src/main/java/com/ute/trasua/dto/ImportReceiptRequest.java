package com.ute.trasua.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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
public class ImportReceiptRequest {

    private Long branchId;

    @Builder.Default
    private String supplierType = "CENTRAL_WAREHOUSE";

    @Size(max = 150, message = "Tên nhà cung cấp tối đa 150 ký tự")
    private String supplierName;

    @NotBlank(message = "Họ tên người giao hàng không được để trống")
    @Size(max = 100, message = "Họ tên người giao hàng tối đa 100 ký tự")
    private String deliveredBy;

    @NotBlank(message = "Họ tên người nhận hàng không được để trống")
    @Size(max = 100, message = "Họ tên người nhận hàng tối đa 100 ký tự")
    private String receivedBy;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;

    @NotEmpty(message = "Danh sách nguyên liệu nhập không được rỗng")
    @Valid
    @Builder.Default
    private List<ImportReceiptItemRequest> items = new ArrayList<>();
}
