package com.ute.trasua.repository;

import com.ute.trasua.entity.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToppingRepository extends JpaRepository<Topping, Long> {

    boolean existsByToppingName(String toppingName);

    boolean existsByToppingNameAndIdNot(String toppingName, Long id);

    @Query("SELECT t FROM Topping t ORDER BY t.price ASC")
    List<Topping> findAllOrderByPriceAsc();

    @Query("SELECT t FROM Topping t WHERE t.status = 'ACTIVE' OR t.status = 'HoatDong' ORDER BY t.price ASC")
    List<Topping> findActiveOrderByPriceAsc();

    Optional<Topping> findByToppingName(String toppingName);

    @Query("SELECT t FROM Topping t WHERE t.status = :status ORDER BY t.toppingName ASC")
    List<Topping> findByStatusOrderByNameAsc(@Param("status") String status);

    default Optional<Topping> findByName(String name) {
        return findByToppingName(name);
    }
}
