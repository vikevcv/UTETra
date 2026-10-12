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
    List<Order> findByBranchIdAndStatusInOrderByCreatedAtAsc(Long branchId, List<String> statuses);
    List<Order> findByBranchIdAndStatusOrderByCreatedAtAsc(Long branchId, String status);
    List<Order> findTop30ByBranchIdAndStatusInOrderByCreatedAtDesc(Long branchId, List<String> statuses);
}
