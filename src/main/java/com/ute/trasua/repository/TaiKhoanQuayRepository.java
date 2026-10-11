package com.ute.trasua.repository;

import com.ute.trasua.entity.TaiKhoanQuay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaiKhoanQuayRepository extends JpaRepository<TaiKhoanQuay, Long> {

    Optional<TaiKhoanQuay> findByTenDangNhap(String tenDangNhap);

    boolean existsByTenDangNhap(String tenDangNhap);

    boolean existsByTenDangNhapAndIdNot(String tenDangNhap, Long id);

    List<TaiKhoanQuay> findByChiNhanhId(Long chiNhanhId);

    List<TaiKhoanQuay> findByChiNhanhIdAndActiveTrue(Long chiNhanhId);

    long countByChiNhanhId(Long chiNhanhId);
}
