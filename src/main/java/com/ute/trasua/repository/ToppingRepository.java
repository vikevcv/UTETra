package com.ute.trasua.repository;

import com.ute.trasua.entity.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToppingRepository extends JpaRepository<Topping, Long> {

    boolean existsByTenTopping(String tenTopping);

    boolean existsByTenToppingAndMaToppingNot(String tenTopping, Long maTopping);

    @Query("SELECT t FROM Topping t ORDER BY t.donGia ASC")
    List<Topping> findAllOrderByDonGiaAsc();

    @Query("SELECT t FROM Topping t WHERE t.trangThai = 'HoatDong' ORDER BY t.donGia ASC")
    List<Topping> findActiveOrderByDonGiaAsc();
}
