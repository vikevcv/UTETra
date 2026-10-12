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
 * REST API cung cấp dữ liệu tùy chọn Kích cỡ & Topping (UC04) và thao tác nhanh qua AJAX (UC23).
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
     * API cập nhật nhanh trạng thái bật/tắt kích cỡ qua AJAX.
     */
    @PatchMapping("/size/{code}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleSizeStatus(@PathVariable String code) {
        boolean newStatus = optionService.toggleSizeStatus(code);
        Map<String, Object> res = new HashMap<>();
        res.put("code", code);
        res.put("active", newStatus);
        res.put("message", newStatus ? "Đã chuyển kích cỡ sang trạng thái Đang bán" : "Đã chuyển kích cỡ sang trạng thái Ngừng bán");
        return ResponseEntity.ok(res);
    }

    /**
     * API cập nhật nhanh trạng thái bật/tắt topping qua AJAX.
     */
    @PatchMapping("/topping/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleToppingStatus(@PathVariable Long id) {
        boolean newStatus = optionService.toggleToppingStatus(id);
        Map<String, Object> res = new HashMap<>();
        res.put("id", id);
        res.put("active", newStatus);
        res.put("message", newStatus ? "Đã chuyển topping sang trạng thái Đang bán" : "Đã chuyển topping sang trạng thái Ngừng bán");
        return ResponseEntity.ok(res);
    }
}
