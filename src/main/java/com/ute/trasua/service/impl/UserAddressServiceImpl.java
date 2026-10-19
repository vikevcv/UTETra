package com.ute.trasua.service.impl;

import com.ute.trasua.dto.user.UserAddressDto;
import com.ute.trasua.dto.user.UserAddressRequest;
import com.ute.trasua.entity.User;
import com.ute.trasua.entity.UserAddress;
import com.ute.trasua.repository.UserAddressRepository;
import com.ute.trasua.repository.UserRepository;
import com.ute.trasua.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserAddressDto> getAddressesByUserId(Long userId) {
        return userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserAddressDto getAddressById(Long addressId, Long userId) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ với ID: " + addressId));
        return mapToDto(address);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAddressDto getDefaultAddress(Long userId) {
        return userAddressRepository.findFirstByUserIdAndIsDefaultTrue(userId)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public UserAddressDto createAddress(Long userId, UserAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        long existingCount = userAddressRepository.countByUserId(userId);
        boolean shouldBeDefault = request.isDefault() || existingCount == 0;

        if (shouldBeDefault) {
            userAddressRepository.clearDefaultForUser(userId);
        }

        UserAddress address = UserAddress.builder()
                .user(user)
                .label(request.getLabel().trim())
                .recipientName(request.getRecipientName().trim())
                .recipientPhone(request.getRecipientPhone().trim())
                .addressDetail(request.getAddressDetail().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isDefault(shouldBeDefault)
                .build();

        UserAddress saved = userAddressRepository.save(address);
        log.info(">>> Đã lưu địa chỉ mới ID {} cho người dùng {}", saved.getId(), user.getUsername());
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public UserAddressDto updateAddress(Long addressId, Long userId, UserAddressRequest request) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ với ID: " + addressId));

        if (request.isDefault() && !address.isDefault()) {
            userAddressRepository.clearDefaultForUser(userId);
            address.setDefault(true);
        } else if (!request.isDefault() && address.isDefault()) {
            // Nếu người dùng bỏ tích mặc định của địa chỉ đang là mặc định duy nhất
            address.setDefault(false);
        }

        address.setLabel(request.getLabel().trim());
        address.setRecipientName(request.getRecipientName().trim());
        address.setRecipientPhone(request.getRecipientPhone().trim());
        address.setAddressDetail(request.getAddressDetail().trim());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());

        UserAddress saved = userAddressRepository.save(address);
        log.info(">>> Đã cập nhật địa chỉ ID {} cho người dùng ID {}", saved.getId(), userId);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId, Long userId) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ với ID: " + addressId));

        boolean wasDefault = address.isDefault();
        userAddressRepository.delete(address);
        log.info(">>> Đã xóa địa chỉ ID {} của người dùng ID {}", addressId, userId);

        // Nếu vừa xóa địa chỉ mặc định, tự động gán địa chỉ còn lại gần nhất làm mặc định
        if (wasDefault) {
            List<UserAddress> remaining = userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (!remaining.isEmpty()) {
                UserAddress newDefault = remaining.get(0);
                newDefault.setDefault(true);
                userAddressRepository.save(newDefault);
            }
        }
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long addressId, Long userId) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ với ID: " + addressId));

        userAddressRepository.clearDefaultForUser(userId);
        address.setDefault(true);
        userAddressRepository.save(address);
        log.info(">>> Đã đặt địa chỉ ID {} làm mặc định cho người dùng ID {}", addressId, userId);
    }

    private UserAddressDto mapToDto(UserAddress entity) {
        return UserAddressDto.builder()
                .id(entity.getId())
                .label(entity.getLabel())
                .recipientName(entity.getRecipientName())
                .recipientPhone(entity.getRecipientPhone())
                .addressDetail(entity.getAddressDetail())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .isDefault(entity.isDefault())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
