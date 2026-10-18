package com.ute.trasua.service;

import com.ute.trasua.dto.review.ProductReviewDto;
import com.ute.trasua.dto.review.ReviewSubmitRequest;
import com.ute.trasua.entity.User;

import java.util.List;

public interface ProductReviewService {

    List<ProductReviewDto> submitOrderReviews(String orderCode, ReviewSubmitRequest request, User currentUser);

    List<ProductReviewDto> getOrderReviews(String orderCode);

    List<ProductReviewDto> getProductReviews(Long productId, int limit);

    List<ProductReviewDto> getAllReviews();

    void toggleHideReview(Long reviewId, boolean isHidden);
}
