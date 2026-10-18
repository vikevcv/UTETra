package com.ute.trasua.service.impl;

import com.ute.trasua.dto.review.ProductReviewDto;
import com.ute.trasua.dto.review.ReviewItemRequest;
import com.ute.trasua.dto.review.ReviewSubmitRequest;
import com.ute.trasua.entity.*;
import com.ute.trasua.repository.OrderDetailRepository;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.repository.ProductRepository;
import com.ute.trasua.repository.ProductReviewRepository;
import com.ute.trasua.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository productReviewRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    @Transactional
    public List<ProductReviewDto> submitOrderReviews(String orderCode, ReviewSubmitRequest request, User currentUser) {
        if (orderCode == null || orderCode.isBlank()) {
            throw new IllegalArgumentException("Mã đơn hàng không hợp lệ.");
        }
        if (request == null || request.getReviews() == null || request.getReviews().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một món để đánh giá.");
        }

        Order order = orderRepository.findByOrderCode(orderCode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng: " + orderCode));

        // 1. Kiểm tra trạng thái đơn: Bắt buộc phải là COMPLETED hoặc DELIVERED
        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "";
        if (!"COMPLETED".equals(status) && !"DELIVERED".equals(status)) {
            throw new IllegalStateException("Đơn hàng chưa hoàn tất, chưa thể đánh giá sản phẩm.");
        }

        // 2. Kiểm tra quyền sở hữu (nếu đơn gắn tài khoản)
        if (currentUser != null && order.getUser() != null) {
            boolean isOwner = order.getUser().getId().equals(currentUser.getId());
            boolean isStaff = currentUser.getRoles() != null && currentUser.getRoles().stream()
                    .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r) || "ROLE_MANAGER".equalsIgnoreCase(r));
            if (!isOwner && !isStaff) {
                throw new IllegalStateException("Bạn không có quyền đánh giá đơn hàng này.");
            }
        }

        String customerName = "Khách hàng";
        if (currentUser != null && currentUser.getFullName() != null && !currentUser.getFullName().isBlank()) {
            customerName = currentUser.getFullName().trim();
        } else if (order.getCustomerName() != null && !order.getCustomerName().isBlank()) {
            customerName = order.getCustomerName().trim();
        } else if (request.getCustomerName() != null && !request.getCustomerName().isBlank()) {
            customerName = request.getCustomerName().trim();
        }

        // Tạo map order detail của đơn để đối soát
        Map<Long, OrderDetail> detailMap = order.getOrderDetails().stream()
                .collect(Collectors.toMap(OrderDetail::getId, d -> d));

        Set<Long> affectedProductIds = new HashSet<>();
        List<ProductReview> savedReviews = new ArrayList<>();

        for (ReviewItemRequest itemReq : request.getReviews()) {
            Long detailId = itemReq.getOrderDetailId();
            if (detailId == null || !detailMap.containsKey(detailId)) {
                continue; // Bỏ qua món không thuộc đơn
            }

            // Kiểm tra chống spam / đánh giá trùng lặp
            if (productReviewRepository.existsByOrderIdAndOrderDetailId(order.getId(), detailId)) {
                log.warn("Món #{} trong đơn {} đã được đánh giá trước đó, bỏ qua.", detailId, orderCode);
                continue;
            }

            OrderDetail detail = detailMap.get(detailId);
            Product product = detail.getProduct();
            if (product == null && itemReq.getProductId() != null) {
                product = productRepository.findById(itemReq.getProductId()).orElse(null);
            }
            if (product == null) {
                continue;
            }

            int rating = itemReq.getRating() != null ? itemReq.getRating() : 5;
            rating = Math.max(1, Math.min(5, rating)); // Clamp 1 to 5

            String comment = itemReq.getComment() != null ? itemReq.getComment().trim() : "";
            if (comment.length() > 500) {
                comment = comment.substring(0, 500);
            }

            ProductReview review = ProductReview.builder()
                    .order(order)
                    .orderDetail(detail)
                    .product(product)
                    .user(currentUser != null ? currentUser : order.getUser())
                    .customerName(customerName)
                    .rating(rating)
                    .comment(comment)
                    .isHidden(false)
                    .build();

            savedReviews.add(productReviewRepository.save(review));
            affectedProductIds.add(product.getId());
        }

        // 3. Cập nhật lại averageRating và reviewCount cho các sản phẩm bị ảnh hưởng
        for (Long pId : affectedProductIds) {
            updateProductRatingStats(pId);
        }

        log.info(">>> Đã lưu {} đánh giá cho đơn hàng {}", savedReviews.size(), orderCode);
        return savedReviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private void updateProductRatingStats(Long productId) {
        try {
            Double avg = productReviewRepository.calculateAverageRatingByProductId(productId);
            long count = productReviewRepository.countByProductIdAndIsHiddenFalse(productId);

            productRepository.findById(productId).ifPresent(p -> {
                double roundedAvg = (avg != null)
                        ? BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue()
                        : 5.0;
                p.setAverageRating(roundedAvg);
                p.setReviewCount((int) count);
                productRepository.save(p);
                log.debug("Cập nhật rating sản phẩm #{}: {} sao ({} đánh giá)", productId, roundedAvg, count);
            });
        } catch (Exception e) {
            log.error("Lỗi cập nhật rating sản phẩm #{}: {}", productId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReviewDto> getOrderReviews(String orderCode) {
        if (orderCode == null || orderCode.isBlank()) {
            return Collections.emptyList();
        }
        Order order = orderRepository.findByOrderCode(orderCode.trim()).orElse(null);
        if (order == null) {
            return Collections.emptyList();
        }
        return productReviewRepository.findByOrderId(order.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReviewDto> getProductReviews(Long productId, int limit) {
        if (productId == null) {
            return Collections.emptyList();
        }
        int pageSize = limit > 0 ? Math.min(limit, 50) : 10;
        return productReviewRepository.findByProductIdAndIsHiddenFalseOrderByCreatedAtDesc(productId, PageRequest.of(0, pageSize))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReviewDto> getAllReviews() {
        return productReviewRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleHideReview(Long reviewId, boolean isHidden) {
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đánh giá ID: " + reviewId));
        review.setIsHidden(isHidden);
        productReviewRepository.save(review);
        if (review.getProduct() != null) {
            updateProductRatingStats(review.getProduct().getId());
        }
        log.info(">>> Đã đổi trạng thái ẩn đánh giá #{} sang: {}", reviewId, isHidden);
    }

    private ProductReviewDto mapToDto(ProductReview review) {
        if (review == null) return null;
        String formattedDate = review.getCreatedAt() != null ? review.getCreatedAt().format(DATE_TIME_FORMATTER) : "";
        return ProductReviewDto.builder()
                .id(review.getId())
                .orderId(review.getOrder() != null ? review.getOrder().getId() : null)
                .orderDetailId(review.getOrderDetail() != null ? review.getOrderDetail().getId() : null)
                .productId(review.getProduct() != null ? review.getProduct().getId() : null)
                .productName(review.getProduct() != null ? review.getProduct().getName() : "")
                .customerName(review.getCustomerName())
                .rating(review.getRating())
                .comment(review.getComment())
                .isHidden(review.getIsHidden())
                .createdAt(review.getCreatedAt())
                .formattedCreatedAt(formattedDate)
                .build();
    }
}
