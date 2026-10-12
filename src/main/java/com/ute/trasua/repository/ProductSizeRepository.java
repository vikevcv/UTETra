package com.ute.trasua.repository;

import com.ute.trasua.entity.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, String> {

    boolean existsBySizeCode(String sizeCode);

    boolean existsBySizeName(String sizeName);

    boolean existsBySizeNameAndSizeCodeNot(String sizeName, String sizeCode);

    @Query("SELECT s FROM ProductSize s ORDER BY s.additionalPrice ASC")
    List<ProductSize> findAllOrderByAdditionalPriceAsc();

    @Query("SELECT s FROM ProductSize s WHERE s.status = 'ACTIVE' OR s.status = 'HoatDong' ORDER BY s.additionalPrice ASC")
    List<ProductSize> findActiveOrderByAdditionalPriceAsc();
}
