package com.ute.trasua.security;

import com.ute.trasua.entity.User;
import com.ute.trasua.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        if (username == null || username.trim().isEmpty()) {
            throw new UsernameNotFoundException("Tên đăng nhập không được để trống");
        }

        String cleanIdentifier = username.trim();
        if (cleanIdentifier.startsWith("@")) {
            cleanIdentifier = cleanIdentifier.substring(1).trim();
        }

        final String searchKey = cleanIdentifier;
        User user = userRepository.findByUsername(searchKey)
                .or(() -> userRepository.findByEmail(searchKey))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy user với tên đăng nhập hoặc email: " + searchKey));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                !user.isLocked(),
                user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );
    }
}