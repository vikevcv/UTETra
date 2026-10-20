package com.ute.trasua.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Request payload for customer order lookup by code or phone number.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLookupRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderCode;
    private String phoneNumber;
}
