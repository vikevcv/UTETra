package com.ute.trasua.repository;

import com.ute.trasua.entity.PosAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PosAccountRepository extends JpaRepository<PosAccount, Long> {

    Optional<PosAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    List<PosAccount> findByBranchId(Long branchId);

    List<PosAccount> findByBranchIdAndActiveTrue(Long branchId);

    long countByBranchId(Long branchId);
}
