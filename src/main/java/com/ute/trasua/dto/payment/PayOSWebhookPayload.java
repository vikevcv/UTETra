package com.ute.trasua.dto.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload JSON mà máy chủ PayOS gửi tới Webhook IPN endpoint (UC06).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayOSWebhookPayload {

    private String code;
    private String desc;
    private PayOSWebhookData data;
    private String signature;
}
