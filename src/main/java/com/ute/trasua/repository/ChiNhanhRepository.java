package com.ute.trasua.repository;

import com.ute.trasua.entity.ChiNhanh;
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
public interface ChiNhanhRepository extends JpaRepository<ChiNhanh, Long> {

    Optional<ChiNhanh> findByMaChiNhanh(String maChiNhanh);

    boolean existsByMaChiNhanh(String maChiNhanh);

    boolean existsByMaChiNhanhAndIdNot(String maChiNhanh, Long id);

    List<ChiNhanh> findByActiveTrue();

    List<ChiNhanh> findByManager(User manager);

    List<ChiNhanh> findByManagerId(Long managerId);

    Optional<ChiNhanh> findFirstByManagerUsername(String username);

    @Query("SELECT c FROM ChiNhanh c LEFT JOIN FETCH c.manager WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(c.maChiNhanh) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(c.tenChiNhanh) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(c.diaChi) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(c.hotline) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:active IS NULL OR c.active = :active)")
    Page<ChiNhanh> searchChiNhanh(@Param("keyword") String keyword,
                                 @Param("active") Boolean active,
                                 Pageable pageable);

    long countByActiveTrue();
}
