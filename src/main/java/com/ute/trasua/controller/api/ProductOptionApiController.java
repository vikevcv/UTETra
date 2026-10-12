package com.ute.trasua.controller.api;

import com.ute.trasua.dto.ProductOptionDto;
import com.ute.trasua.entity.ProductOptionType;
import com.ute.trasua.service.ProductOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API cung cấp dữ liệu tùy chọn cho Client (UC04) và thao tác nhanh qua AJAX.
 */
@RestController
@RequestMapping("/api/v1/product-options")
@RequiredArgsConstructor
public class ProductOptionApiController {

    private final ProductOptionService optionService;

    /**
     * API công khai trả về danh sách Size và Topping đang hoạt động để modal đặt món (UC04) tải động.
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, List<ProductOptionDto>>> getActiveOptions() {
        Map<String, List<ProductOptionDto>> response = new HashMap<>();
        response.put("sizes", optionService.getActiveOptionsByType(ProductOptionType.SIZE));
        response.put("toppings", optionService.getActiveOptionsByType(ProductOptionType.TOPPING));
        return ResponseEntity.ok(response);
    }

    /**
     * API cập nhật nhanh trạng thái bật/tắt kinh doanh qua AJAX Switch trên giao diện Admin.
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleStatus(@PathVariable Long id) {
        boolean newStatus = optionService.toggleActiveStatus(id);
        Map<String, Object> res = new HashMap<>();
        res.put("id", id);
        res.put("active", newStatus);
        res.put("message", newStatus ? "Đã chuyển sang trạng thái Đang bán" : "Đã chuyển sang trạng thái Ngừng bán");
        return ResponseEntity.ok(res);
    }
}
