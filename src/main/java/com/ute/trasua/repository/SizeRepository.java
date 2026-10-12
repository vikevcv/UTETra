package com.ute.trasua.repository;

import com.ute.trasua.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {
    List<Size> findAllByActiveTrueOrderByExtraPriceAsc();
    Optional<Size> findByName(String name);
}
