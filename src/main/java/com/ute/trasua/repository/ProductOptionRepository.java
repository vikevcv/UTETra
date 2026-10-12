package com.ute.trasua.repository;

import com.ute.trasua.entity.ProductOption;
import com.ute.trasua.entity.ProductOptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    boolean existsByOptionCode(String optionCode);

    boolean existsByOptionCodeAndIdNot(String optionCode, Long id);

    Optional<ProductOption> findByOptionCode(String optionCode);

    List<ProductOption> findByTypeOrderByAdditionalPriceAsc(ProductOptionType type);

    List<ProductOption> findByTypeAndActiveTrueOrderByAdditionalPriceAsc(ProductOptionType type);

    @Query("SELECT p FROM ProductOption p WHERE " +
           "(:type IS NULL OR p.type = :type) AND " +
           "(:active IS NULL OR p.active = :active) AND " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(p.optionCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.additionalPrice ASC")
    List<ProductOption> searchOptions(@Param("type") ProductOptionType type,
                                      @Param("active") Boolean active,
                                      @Param("keyword") String keyword);

    long countByTypeAndActiveTrue(ProductOptionType type);
}
