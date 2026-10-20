package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.kitchen.CupStickerDto;
import com.ute.trasua.dto.kitchen.KitchenOrderDto;
import com.ute.trasua.service.KitchenService;
import com.ute.trasua.service.StickerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API cho màn hình Pha Chế Barista KDS (UC08, UC09).
 */
@RestController
@RequestMapping("/api/kitchen")
@RequiredArgsConstructor
@Slf4j
public class KitchenApiController {

    private final KitchenService kitchenService;
    private final StickerService stickerService;
    private final com.ute.trasua.service.BranchMenuService branchMenuService;

    /**
     * Lấy số lượng tồn kho Ly theo Size tại chi nhánh.
     */
    @GetMapping("/cups")
    public ResponseEntity<java.util.Map<String, Integer>> getBranchCupStocks(
            @RequestParam(value = "branchId", required = false, defaultValue = "1") Long branchId) {
        return ResponseEntity.ok(branchMenuService.getBranchCupStocks(branchId));
    }

    /**
     * Lấy danh sách hàng đợi các đơn hàng cần pha chế tại chi nhánh.
     */
    @GetMapping("/orders")
    public ResponseEntity<List<KitchenOrderDto>> getKitchenOrders(
            @RequestParam(value = "branchId", required = false, defaultValue = "1") Long branchId) {
        List<KitchenOrderDto> orders = kitchenService.getKitchenOrders(branchId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Barista tiếp nhận đơn hàng và bắt đầu pha chế (UC08).
     */
    @PostMapping("/orders/{orderId}/accept")
    public ResponseEntity<KitchenOrderDto> acceptOrderForPreparation(@PathVariable Long orderId) {
        KitchenOrderDto updated = kitchenService.acceptOrderForPreparation(orderId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Barista hoàn tất pha chế đồ uống (UC08).
     */
    @PostMapping("/orders/{orderId}/complete")
    public ResponseEntity<KitchenOrderDto> completePreparation(@PathVariable Long orderId) {
        KitchenOrderDto updated = kitchenService.completePreparation(orderId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Sinh và lấy toàn bộ danh sách tem dán ly theo biểu mẫu QLPC_BM1 (UC09).
     */
    @GetMapping("/orders/{orderId}/stickers")
    public ResponseEntity<List<CupStickerDto>> printCupStickers(@PathVariable Long orderId) {
        List<CupStickerDto> stickers = stickerService.printCupStickers(orderId);
        return ResponseEntity.ok(stickers);
    }

    /**
     * In lại đúng một tem dán ly cụ thể khi kẹt giấy / lỗi in (UC09).
     */
    @GetMapping("/orders/{orderId}/stickers/{cupIndex}")
    public ResponseEntity<CupStickerDto> printSingleCupSticker(
            @PathVariable Long orderId,
            @PathVariable Integer cupIndex) {
        CupStickerDto sticker = stickerService.printSingleCupSticker(orderId, cupIndex);
        return ResponseEntity.ok(sticker);
    }
}
