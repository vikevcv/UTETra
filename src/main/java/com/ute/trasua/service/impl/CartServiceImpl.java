package com.ute.trasua.service.impl;

import com.ute.trasua.dto.*;
import com.ute.trasua.entity.Product;
import com.ute.trasua.entity.ProductSize;
import com.ute.trasua.entity.Topping;
import com.ute.trasua.repository.ProductRepository;
import com.ute.trasua.repository.ProductSizeRepository;
import com.ute.trasua.repository.ToppingRepository;
import com.ute.trasua.service.BranchMenuService;
import com.ute.trasua.service.CartService;
import com.ute.trasua.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý tính toán và chuẩn hóa giỏ hàng (Module 1 - UC04).
 * Được tối ưu hóa truy vấn hàng loạt (Batch Query) để tránh N+1 Query
 * và phân tách thành các phương thức độc lập, dễ kiểm thử và mở rộng.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ToppingRepository toppingRepository;
    private final BranchMenuService branchMenuService;
    private final PromotionService promotionService;

    // Helper records lưu trữ kết quả phân giải nhỏ gọn, immutable
    private record CartLookupContext(Map<Long, Product> productMap,
                                     Map<String, ProductSize> sizeMap,
                                     Map<Long, Topping> toppingMap) {}

    private record SizeInfo(String sizeCode, String sizeName, BigDecimal extraPrice) {}

    private record ToppingsInfo(List<CartItemToppingDto> validatedToppings,
                                BigDecimal toppingsSum,
                                List<Long> toppingIds) {}

    private record VoucherDiscountResult(String appliedCode, BigDecimal discountAmount) {}

    /**
     * 1. Xác thực và tính giá cho một ly trà sữa đơn lẻ (Product + Size + Toppings + Chi nhánh)
     */
    @Override
    @Transactional(readOnly = true)
    public CartItemDto validateAndPriceItem(CartItemDto rawItem, Long branchId) {
        if (rawItem == null || rawItem.getProductId() == null) {
            return null;
        }
        CartLookupContext context = prepareLookupContext(List.of(rawItem));
        List<String> warnings = new ArrayList<>();
        return processCartItem(rawItem, branchId, context, warnings);
    }

    /**
     * 2. Xác thực và tính toán giá trị của toàn bộ danh sách món trong giỏ hàng (Batch Fetching)
     */
    @Override
    @Transactional(readOnly = true)
    public CartDto validateCartItems(List<CartItemDto> items, Long branchId) {
        if (items == null || items.isEmpty()) {
            return createEmptyCart();
        }

        List<String> warnings = new ArrayList<>();
        CartLookupContext context = prepareLookupContext(items);

        List<CartItemDto> validatedItems = new ArrayList<>();
        int totalQty = 0;
        BigDecimal subtotalSum = BigDecimal.ZERO;

        for (CartItemDto rawItem : items) {
            CartItemDto itemDto = processCartItem(rawItem, branchId, context, warnings);
            if (itemDto != null) {
                validatedItems.add(itemDto);
                totalQty += itemDto.getQuantity();
                subtotalSum = subtotalSum.add(itemDto.getSubtotal());
            }
        }

        return CartDto.builder()
                .items(validatedItems)
                .totalQuantity(totalQty)
                .subtotalAmount(subtotalSum)
                .discountAmount(BigDecimal.ZERO)
                .appliedVoucherCode(null)
                .finalAmount(subtotalSum)
                .warnings(warnings)
                .build();
    }

    /**
     * 3. Áp dụng mã Voucher khuyến mãi vào giỏ hàng đã được định giá
     */
    @Override
    @Transactional(readOnly = true)
    public CartDto applyVoucherToCart(CartDto cart, String voucherCode, Long branchId) {
        if (cart == null) {
            return createEmptyCart();
        }

        List<String> warnings = new ArrayList<>(cart.getWarnings() != null ? cart.getWarnings() : Collections.emptyList());
        VoucherDiscountResult voucherResult = applyVoucherDiscount(
                voucherCode, branchId, cart.getSubtotalAmount(), warnings);

        BigDecimal discount = voucherResult.discountAmount();
        BigDecimal finalAmount = cart.getSubtotalAmount().subtract(discount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        return CartDto.builder()
                .items(cart.getItems())
                .totalQuantity(cart.getTotalQuantity())
                .subtotalAmount(cart.getSubtotalAmount())
                .discountAmount(discount)
                .appliedVoucherCode(voucherResult.appliedCode())
                .finalAmount(finalAmount)
                .warnings(warnings)
                .build();
    }

    /**
     * 4. Gỡ bỏ mã Voucher ra khỏi giỏ hàng và khôi phục nguyên giá đơn hàng
     */
    @Override
    public CartDto removeVoucherFromCart(CartDto cart) {
        if (cart == null) {
            return createEmptyCart();
        }
        return CartDto.builder()
                .items(cart.getItems())
                .totalQuantity(cart.getTotalQuantity())
                .subtotalAmount(cart.getSubtotalAmount())
                .discountAmount(BigDecimal.ZERO)
                .appliedVoucherCode(null)
                .finalAmount(cart.getSubtotalAmount())
                .warnings(cart.getWarnings())
                .build();
    }

    /**
     * 5. Phương thức điều phối toàn diện:
     * Bước 1: validateCartItems -> Bước 2: applyVoucherToCart
     */
    @Override
    @Transactional(readOnly = true)
    public CartDto validateAndCalculateCart(CartValidateRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return createEmptyCart();
        }

        // Bước 1: Xác thực danh sách sản phẩm & tính subtotal bằng Batch Query
        CartDto validatedCart = validateCartItems(request.getItems(), request.getBranchId());

        // Bước 2: Áp dụng voucher khuyến mãi nếu có
        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            return applyVoucherToCart(validatedCart, request.getVoucherCode(), request.getBranchId());
        }

        return validatedCart;
    }

    /**
     * Tạo giỏ hàng rỗng mặc định
     */
    private CartDto createEmptyCart() {
        return CartDto.builder()
                .items(Collections.emptyList())
                .totalQuantity(0)
                .subtotalAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.ZERO)
                .warnings(Collections.emptyList())
                .build();
    }

    /**
     * Nạp trước toàn bộ Sản phẩm, Kích cỡ và Topping bằng truy vấn theo lô (Batch Fetching),
     * giải quyết hoàn toàn vấn đề N+1 Database Query khi giỏ hàng có nhiều món.
     */
    private CartLookupContext prepareLookupContext(List<CartItemDto> rawItems) {
        Set<Long> productIds = rawItems.stream()
                .map(CartItemDto::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> toppingIds = rawItems.stream()
                .filter(item -> item.getToppings() != null)
                .flatMap(item -> item.getToppings().stream())
                .map(CartItemToppingDto::getToppingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        Map<String, ProductSize> sizeMap = productSizeRepository.findAll().stream()
                .collect(Collectors.toMap(s -> s.getSizeCode().toUpperCase(), s -> s));

        Map<Long, Topping> toppingMap = toppingRepository.findAllById(toppingIds).stream()
                .collect(Collectors.toMap(Topping::getId, t -> t));

        return new CartLookupContext(productMap, sizeMap, toppingMap);
    }

    /**
     * Xác thực và đóng gói thông tin chi tiết cho từng ly trà sữa
     */
    private CartItemDto processCartItem(CartItemDto rawItem,
                                        Long branchId,
                                        CartLookupContext context,
                                        List<String> warnings) {
        if (rawItem.getProductId() == null) {
            return null;
        }

        Product product = context.productMap().get(rawItem.getProductId());
        if (product == null || !"ACTIVE".equalsIgnoreCase(product.getStatus())) {
            warnings.add("Phẩm trà '" + (rawItem.getProductName() != null ? rawItem.getProductName() : "Đã chọn") + "' hiện không còn kinh doanh.");
            return null;
        }

        BigDecimal basePrice = product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO;

        // 1. Phân giải kích cỡ
        SizeInfo sizeInfo = resolveSize(rawItem.getSizeCode(), rawItem.getSizeName(), context.sizeMap());

        // 2. Phân giải danh sách Topping
        ToppingsInfo toppingsInfo = resolveToppings(rawItem.getToppings(), context.toppingMap(), warnings);

        // 3. Chuẩn hóa đường, đá, ghi chú, số lượng
        String sugar = (rawItem.getSugarLevel() != null && !rawItem.getSugarLevel().isBlank()) ? rawItem.getSugarLevel().trim() : "100%";
        String ice = (rawItem.getIceLevel() != null && !rawItem.getIceLevel().isBlank()) ? rawItem.getIceLevel().trim() : "100%";
        String note = (rawItem.getNote() != null) ? rawItem.getNote().trim() : "";
        int qty = (rawItem.getQuantity() != null && rawItem.getQuantity() > 0) ? rawItem.getQuantity() : 1;

        // 4. Tính đơn giá và thành tiền
        BigDecimal unitPrice = basePrice.add(sizeInfo.extraPrice()).add(toppingsInfo.toppingsSum());
        BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(qty));

        // 5. Kiểm tra tình trạng tại chi nhánh
        boolean isAvailable = checkBranchAvailability(branchId, product.getId(), product.getName(), warnings);

        // 6. Sinh itemKey duy nhất cho ly trà
        String itemKey = generateItemKey(product.getId(), sizeInfo.sizeCode(), sugar, ice, toppingsInfo.toppingIds(), note);

        return CartItemDto.builder()
                .itemKey(itemKey)
                .productId(product.getId())
                .productName(product.getName())
                .productCode(product.getProductCode())
                .imageUrl(product.getImageUrl())
                .basePrice(basePrice)
                .sizeCode(sizeInfo.sizeCode())
                .sizeName(sizeInfo.sizeName())
                .sizeExtraPrice(sizeInfo.extraPrice())
                .sugarLevel(sugar)
                .iceLevel(ice)
                .toppings(toppingsInfo.validatedToppings())
                .quantity(qty)
                .note(note)
                .unitPrice(unitPrice)
                .subtotal(itemSubtotal)
                .available(isAvailable)
                .build();
    }

    /**
     * Xác thực thông tin kích cỡ và giá cộng thêm
     */
    private SizeInfo resolveSize(String rawCode, String rawName, Map<String, ProductSize> sizeMap) {
        String sizeCode = (rawCode != null && !rawCode.isBlank()) ? rawCode.trim().toUpperCase() : "M";
        ProductSize sizeEntity = sizeMap.get(sizeCode);
        if (sizeEntity != null) {
            BigDecimal extra = sizeEntity.getAdditionalPrice() != null ? sizeEntity.getAdditionalPrice() : BigDecimal.ZERO;
            return new SizeInfo(sizeCode, sizeEntity.getSizeName(), extra);
        }
        String fallbackName = (rawName != null && !rawName.isBlank()) ? rawName : "Chén Vừa (Size " + sizeCode + ")";
        return new SizeInfo(sizeCode, fallbackName, BigDecimal.ZERO);
    }

    /**
     * Xác thực danh sách Topping hợp lệ và còn hàng
     */
    private ToppingsInfo resolveToppings(List<CartItemToppingDto> rawToppings,
                                         Map<Long, Topping> toppingMap,
                                         List<String> warnings) {
        if (rawToppings == null || rawToppings.isEmpty()) {
            return new ToppingsInfo(Collections.emptyList(), BigDecimal.ZERO, Collections.emptyList());
        }

        List<CartItemToppingDto> validated = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;
        List<Long> ids = new ArrayList<>();

        for (CartItemToppingDto rawTop : rawToppings) {
            if (rawTop.getToppingId() == null) continue;
            Topping topping = toppingMap.get(rawTop.getToppingId());
            if (topping != null && topping.isActive()) {
                BigDecimal price = topping.getPrice() != null ? topping.getPrice() : BigDecimal.ZERO;
                validated.add(CartItemToppingDto.builder()
                        .toppingId(topping.getId())
                        .toppingName(topping.getToppingName())
                        .price(price)
                        .build());
                sum = sum.add(price);
                ids.add(topping.getId());
            } else {
                warnings.add("Topping '" + (rawTop.getToppingName() != null ? rawTop.getToppingName() : "tùy chọn") + "' tạm thời hết hàng.");
            }
        }

        return new ToppingsInfo(validated, sum, ids);
    }

    /**
     * Kiểm tra trạng thái kinh doanh của món tại chi nhánh cụ thể
     */
    private boolean checkBranchAvailability(Long branchId, Long productId, String productName, List<String> warnings) {
        if (branchId == null) {
            return true;
        }
        boolean available = branchMenuService.isProductAvailable(branchId, productId);
        if (!available) {
            warnings.add("Phẩm trà '" + productName + "' tạm hết tại cơ sở này.");
        }
        return available;
    }

    /**
     * Sinh khóa băm (itemKey) đồng bộ với client-side
     */
    private String generateItemKey(Long productId,
                                   String sizeCode,
                                   String sugar,
                                   String ice,
                                   List<Long> toppingIds,
                                   String note) {
        List<Long> sortedToppingIds = new ArrayList<>(toppingIds);
        Collections.sort(sortedToppingIds);
        return productId + "_" + sizeCode + "_" + sugar + "_" + ice + "_" + sortedToppingIds + "_" + note;
    }

    /**
     * Kiểm tra và tính toán giảm giá từ Voucher khuyến mãi
     */
    private VoucherDiscountResult applyVoucherDiscount(String voucherCode,
                                                       Long branchId,
                                                       BigDecimal subtotal,
                                                       List<String> warnings) {
        if (voucherCode == null || voucherCode.trim().isEmpty() || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return new VoucherDiscountResult(null, BigDecimal.ZERO);
        }

        String cleanCode = voucherCode.trim().toUpperCase();
        try {
            VoucherApplyResponse resp = promotionService.validateAndApplyVoucher(cleanCode, branchId, subtotal);
            if (resp.isValid()) {
                BigDecimal discount = resp.getDiscountAmount() != null ? resp.getDiscountAmount() : BigDecimal.ZERO;
                return new VoucherDiscountResult(cleanCode, discount);
            } else {
                warnings.add("Mã voucher '" + cleanCode + "': " + resp.getMessage());
            }
        } catch (Exception e) {
            log.warn("Lỗi xác thực voucher {}: {}", cleanCode, e.getMessage());
            warnings.add("Không thể áp dụng mã voucher '" + cleanCode + "': " + e.getMessage());
        }

        return new VoucherDiscountResult(null, BigDecimal.ZERO);
    }
}
