package com.ute.trasua.repository;

import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByBranchCode(String branchCode);

    boolean existsByBranchCode(String branchCode);

    boolean existsByBranchCodeAndIdNot(String branchCode, Long id);

    List<Branch> findByActiveTrue();

    List<Branch> findByManager(User manager);

    List<Branch> findByManagerId(Long managerId);

    Optional<Branch> findFirstByManagerUsername(String username);

    @Query("SELECT b FROM Branch b LEFT JOIN FETCH b.manager WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(b.branchCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(b.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(b.hotline) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:active IS NULL OR b.active = :active)")
    Page<Branch> searchBranches(@Param("keyword") String keyword,
                               @Param("active") Boolean active,
                               Pageable pageable);

    long countByActiveTrue();
}
