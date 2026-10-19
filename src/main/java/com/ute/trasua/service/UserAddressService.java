package com.ute.trasua.service;

import com.ute.trasua.dto.user.UserAddressDto;
import com.ute.trasua.dto.user.UserAddressRequest;

import java.util.List;

/**
 * Service quản lý Sổ địa chỉ giao hàng của khách hàng (Delivery Address Book).
 */
public interface UserAddressService {

    List<UserAddressDto> getAddressesByUserId(Long userId);

    UserAddressDto getAddressById(Long addressId, Long userId);

    UserAddressDto getDefaultAddress(Long userId);

    UserAddressDto createAddress(Long userId, UserAddressRequest request);

    UserAddressDto updateAddress(Long addressId, Long userId, UserAddressRequest request);

    void deleteAddress(Long addressId, Long userId);

    void setDefaultAddress(Long addressId, Long userId);
}
