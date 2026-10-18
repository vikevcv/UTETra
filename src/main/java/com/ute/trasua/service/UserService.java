package com.ute.trasua.service;

import com.ute.trasua.entity.User;

import java.util.Optional;

/**
 * Service interface cung cấp các thao tác truy vấn và quản lý User hệ thống.
 */
public interface UserService {

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    User save(User user);

    User getCurrentAuthenticatedUser();
}
