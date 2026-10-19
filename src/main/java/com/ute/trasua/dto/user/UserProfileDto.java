package com.ute.trasua.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String avatar;
    private boolean enabled;
    private LocalDateTime createdAt;
    private Set<String> roles;
}
