package com.ute.trasua.repository;

import com.ute.trasua.entity.OrderComplaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderComplaintRepository extends JpaRepository<OrderComplaint, Long> {

    Optional<OrderComplaint> findByOrderId(Long orderId);

    Optional<OrderComplaint> findByOrder_OrderCode(String orderCode);

    boolean existsByOrderId(Long orderId);

    List<OrderComplaint> findAllByOrderByCreatedAtDesc();

    List<OrderComplaint> findByStatusOrderByCreatedAtDesc(String status);

    List<OrderComplaint> findByOrder_Branch_IdOrderByCreatedAtDesc(Long branchId);

    List<OrderComplaint> findByOrder_Branch_IdAndStatusOrderByCreatedAtDesc(Long branchId, String status);
}
