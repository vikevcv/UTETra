package com.ute.trasua.repository;

import com.ute.trasua.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // KDS / Barista Queries
    List<Order> findByBranchIdAndStatusInOrderByCreatedAtAsc(Long branchId, List<String> statuses);
    List<Order> findByBranchIdAndStatusOrderByCreatedAtAsc(Long branchId, String status);
    List<Order> findTop30ByBranchIdAndStatusInOrderByCreatedAtDesc(Long branchId, List<String> statuses);

    // Handover & Driver / Dispatch Queries
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderDetails " +
           "LEFT JOIN FETCH o.driver d " +
           "LEFT JOIN FETCH d.user " +
           "WHERE (:branchId IS NULL OR o.branch.id = :branchId OR (:branchId = 1L AND o.branch IS NULL)) " +
           "AND o.status IN :statuses " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByBranchIdAndStatusInOrderByCreatedAtDesc(@Param("branchId") Long branchId, @Param("statuses") List<String> statuses);

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderDetails " +
           "WHERE o.driver.id = :driverId AND o.status IN :statuses " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByDriverIdAndStatusInOrderByCreatedAtDesc(@Param("driverId") Long driverId, @Param("statuses") List<String> statuses);

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderDetails " +
           "WHERE o.driver.id = :driverId " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByDriverIdOrderByCreatedAtDesc(@Param("driverId") Long driverId);

    // Revenue Report Queries
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.branch b " +
           "WHERE (:branchId IS NULL OR o.branch.id = :branchId) AND " +
           "o.createdAt >= :start AND o.createdAt <= :end " +
           "ORDER BY o.createdAt ASC")
    List<Order> findOrdersForReport(@Param("branchId") Long branchId,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    @Query("SELECT d.product.id, d.productName, SUM(d.quantity), SUM(d.subtotal) " +
           "FROM OrderDetail d " +
           "JOIN d.order o " +
           "WHERE (:branchId IS NULL OR o.branch.id = :branchId) AND " +
           "o.createdAt >= :start AND o.createdAt <= :end AND " +
           "(o.paymentStatus = 'PAID' OR o.status = 'PAID' OR o.status = 'COMPLETED') " +
           "GROUP BY d.product.id, d.productName " +
           "ORDER BY SUM(d.quantity) DESC")
    List<Object[]> findTopSellingProducts(@Param("branchId") Long branchId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);
}
