package com.ute.trasua.repository;

import com.ute.trasua.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository thao tác với bảng `users`.
 * Đã mở rộng các query phục vụ Module 11 (Quản lý User & Giám sát Tài xế toàn chuỗi).
 */
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * Đếm số lượng người dùng theo trạng thái khóa.
     */
    long countByLocked(boolean locked);

    /**
     * Đếm số lượng người dùng theo vai trò cụ thể (VD: ROLE_DRIVER, ROLE_CUSTOMER, ROLE_STAFF).
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r = :role")
    long countByRole(@Param("role") String role);

    /**
     * Lấy danh sách người dùng theo vai trò có phân trang.
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :role")
    Page<User> findByRole(@Param("role") String role, Pageable pageable);

    /**
     * Tìm kiếm và lọc người dùng đa tiêu chí (từ khóa, vai trò, trạng thái khóa) có phân trang.
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " u.phone LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:role IS NULL OR :role = '' OR r = :role) AND " +
           "(:locked IS NULL OR u.locked = :locked)")
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") String role,
            @Param("locked") Boolean locked,
            Pageable pageable
    );

    /**
     * Tìm kiếm riêng tài xế (ROLE_DRIVER) kèm từ khóa tìm kiếm có phân trang.
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = 'ROLE_DRIVER' AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " u.phone LIKE CONCAT('%', :keyword, '%'))")
    Page<User> searchDrivers(@Param("keyword") String keyword, Pageable pageable);
}