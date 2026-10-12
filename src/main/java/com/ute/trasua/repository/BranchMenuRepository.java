package com.ute.trasua.repository;

import com.ute.trasua.entity.BranchMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchMenuRepository extends JpaRepository<BranchMenu, Long> {

    Optional<BranchMenu> findByBranchIdAndProductId(Long branchId, Long productId);

    List<BranchMenu> findByBranchId(Long branchId);

    List<BranchMenu> findByBranchIdAndIsAvailableFalse(Long branchId);
}
