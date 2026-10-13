package com.ute.trasua.repository;

import com.ute.trasua.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByBranchIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long branchId, LocalDateTime start, LocalDateTime end);
    long countByBranchIdAndCreatedAtBetween(Long branchId, LocalDateTime start, LocalDateTime end);
    List<Order> findTop20ByBranchIdOrderByCreatedAtDesc(Long branchId);

    @org.springframework.data.jpa.repository.Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.branch b " +
           "WHERE (:branchId IS NULL OR o.branch.id = :branchId) AND " +
           "o.createdAt >= :start AND o.createdAt <= :end " +
           "ORDER BY o.createdAt ASC")
    List<Order> findOrdersForReport(@org.springframework.data.repository.query.Param("branchId") Long branchId,
                                    @org.springframework.data.repository.query.Param("start") LocalDateTime start,
                                    @org.springframework.data.repository.query.Param("end") LocalDateTime end);

    @org.springframework.data.jpa.repository.Query("SELECT d.product.id, d.productName, SUM(d.quantity), SUM(d.subtotal) " +
           "FROM OrderDetail d " +
           "JOIN d.order o " +
           "WHERE (:branchId IS NULL OR o.branch.id = :branchId) AND " +
           "o.createdAt >= :start AND o.createdAt <= :end AND " +
           "(o.paymentStatus = 'PAID' OR o.status = 'PAID' OR o.status = 'COMPLETED') " +
           "GROUP BY d.product.id, d.productName " +
           "ORDER BY SUM(d.quantity) DESC")
    List<Object[]> findTopSellingProducts(@org.springframework.data.repository.query.Param("branchId") Long branchId,
                                          @org.springframework.data.repository.query.Param("start") LocalDateTime start,
                                          @org.springframework.data.repository.query.Param("end") LocalDateTime end);
}
