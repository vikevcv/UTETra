package com.ute.trasua.repository;

import com.ute.trasua.entity.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToppingRepository extends JpaRepository<Topping, Long> {
    List<Topping> findByStatusOrderByNameAsc(String status);
    Optional<Topping> findByName(String name);
}
