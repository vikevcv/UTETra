package com.ute.trasua.service;

import com.ute.trasua.dto.kitchen.BranchMenuStatusDto;

import java.util.List;

/**
 * Service xử lý cập nhật trạng thái còn/hết hàng tại chi nhánh (UC11).
 * 100% tuân thủ tên phương thức Tiếng Anh theo đặc tả Báo cáo SE.
 */
public interface BranchMenuService {

    /**
     * Lấy toàn bộ thực đơn món nước và topping của chi nhánh kèm trạng thái còn/hết hàng.
     * @param branchId ID chi nhánh
     * @return Danh sách món và topping
     */
    List<BranchMenuStatusDto> getBranchMenuStatus(Long branchId);

    /**
     * Bật/tắt trạng thái kinh doanh của một sản phẩm tại chi nhánh (UC11).
     * @param branchId ID chi nhánh
     * @param productId ID sản phẩm
     * @param isAvailable true = Còn hàng, false = Tạm hết
     * @return Kết quả thành công
     */
    boolean toggleProductAvailability(Long branchId, Long productId, Boolean isAvailable);

    /**
     * Bật/tắt trạng thái kinh doanh của một topping tại chi nhánh (UC11).
     * @param branchId ID chi nhánh
     * @param toppingId ID topping
     * @param isAvailable true = Còn hàng, false = Tạm hết
     * @return Kết quả thành công
     */
    boolean toggleToppingAvailability(Long branchId, Long toppingId, Boolean isAvailable);

    /**
     * Kiểm tra nhanh một sản phẩm có đang sẵn sàng bán tại chi nhánh hay không.
     * @param branchId ID chi nhánh
     * @param productId ID sản phẩm
     * @return true nếu còn hàng
     */
    boolean isProductAvailable(Long branchId, Long productId);

    /**
     * Lấy số lượng tồn kho Ly theo Size (M, L) tại chi nhánh.
     * @param branchId ID chi nhánh
     * @return Map<Tên Size, Số lượng tồn>
     */
    java.util.Map<String, Integer> getBranchCupStocks(Long branchId);

    /**
     * Trừ tồn kho Ly khi hoàn tất pha chế cốc.
     * @param branchId ID chi nhánh
     * @param sizeName Tên Size (M, L)
     * @param quantity Số lượng ly
     */
    void deductCupStock(Long branchId, String sizeName, int quantity);

    /**
     * Trừ tồn kho món nước tại chi nhánh khi bán/hoàn tất đơn.
     * @param branchId ID chi nhánh
     * @param productId ID món nước
     * @param quantity Số lượng ly món đó
     */
    void deductProductStock(Long branchId, Long productId, int quantity);

    /**
     * Trừ tồn kho Topping tại chi nhánh khi bán/hoàn tất đơn.
     * @param branchId ID chi nhánh
     * @param toppingName Tên Topping
     * @param quantity Số lượng phần topping
     */
    void deductToppingStock(Long branchId, String toppingName, int quantity);
}
