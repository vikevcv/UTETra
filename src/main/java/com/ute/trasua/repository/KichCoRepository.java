package com.ute.trasua.repository;

import com.ute.trasua.entity.KichCo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KichCoRepository extends JpaRepository<KichCo, String> {

    boolean existsByMaKichCo(String maKichCo);

    boolean existsByTenKichCo(String tenKichCo);

    boolean existsByTenKichCoAndMaKichCoNot(String tenKichCo, String maKichCo);

    @Query("SELECT k FROM KichCo k ORDER BY k.giaPhuThu ASC")
    List<KichCo> findAllOrderByGiaPhuThuAsc();

    @Query("SELECT k FROM KichCo k WHERE k.trangThai = 'HoatDong' ORDER BY k.giaPhuThu ASC")
    List<KichCo> findActiveOrderByGiaPhuThuAsc();
}
