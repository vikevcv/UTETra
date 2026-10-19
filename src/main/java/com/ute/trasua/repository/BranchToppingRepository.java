package com.ute.trasua.repository;

import com.ute.trasua.entity.BranchTopping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BranchTopping (UC11).
 * Handles branch-scoped topping availability lookup and upsert.
 */
@Repository
public interface BranchToppingRepository extends JpaRepository<BranchTopping, Long> {
    List<BranchTopping> findByBranchId(Long branchId);
    Optional<BranchTopping> findByBranchIdAndToppingId(Long branchId, Long toppingId);
}
