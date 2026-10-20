package com.ute.trasua.repository;

import com.ute.trasua.entity.ProductReview;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    boolean existsByOrderIdAndOrderDetailId(Long orderId, Long orderDetailId);

    Optional<ProductReview> findByOrderIdAndOrderDetailId(Long orderId, Long orderDetailId);

    List<ProductReview> findByOrderId(Long orderId);

    List<ProductReview> findByProductIdAndIsHiddenFalseOrderByCreatedAtDesc(Long productId, Pageable pageable);

    List<ProductReview> findByProductIdAndRatingAndIsHiddenFalseOrderByCreatedAtDesc(Long productId, Integer rating, Pageable pageable);

    long countByProductIdAndIsHiddenFalse(Long productId);

    long countByProductIdAndRatingAndIsHiddenFalse(Long productId, Integer rating);

    @Query("SELECT r.rating, COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.isHidden = false GROUP BY r.rating")
    List<Object[]> countReviewsGroupedByRating(@Param("productId") Long productId);

    @Query("SELECT COALESCE(AVG(r.rating), 5.0) FROM ProductReview r WHERE r.product.id = :productId AND r.isHidden = false")
    Double calculateAverageRatingByProductId(@Param("productId") Long productId);

    List<ProductReview> findAllByOrderByCreatedAtDesc();
}
