package com.ute.trasua.repository;

import com.ute.trasua.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {

    Optional<NhanVien> findByMaNhanVien(String maNhanVien);

    boolean existsByMaNhanVien(String maNhanVien);

    boolean existsByMaNhanVienAndIdNot(String maNhanVien, Long id);

    List<NhanVien> findByChiNhanhId(Long chiNhanhId);

    List<NhanVien> findByChiNhanhIdAndViTri(Long chiNhanhId, String viTri);

    List<NhanVien> findByChiNhanhIdAndCaLamViec(Long chiNhanhId, String caLamViec);

    List<NhanVien> findByChiNhanhIdAndTrangThai(Long chiNhanhId, String trangThai);

    long countByChiNhanhId(Long chiNhanhId);

    long countByChiNhanhIdAndTrangThai(Long chiNhanhId, String trangThai);

    @Query("SELECT nv FROM NhanVien nv JOIN FETCH nv.chiNhanh cn WHERE " +
           "(:chiNhanhId IS NULL OR cn.id = :chiNhanhId) AND " +
           "(:viTri IS NULL OR :viTri = '' OR nv.viTri = :viTri) AND " +
           "(:caLamViec IS NULL OR :caLamViec = '' OR nv.caLamViec = :caLamViec) AND " +
           "(:trangThai IS NULL OR :trangThai = '' OR nv.trangThai = :trangThai) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(nv.maNhanVien) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(nv.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(nv.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(nv.cccd) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<NhanVien> searchNhanVien(@Param("chiNhanhId") Long chiNhanhId,
                                  @Param("keyword") String keyword,
                                  @Param("viTri") String viTri,
                                  @Param("caLamViec") String caLamViec,
                                  @Param("trangThai") String trangThai,
                                  Pageable pageable);
}
