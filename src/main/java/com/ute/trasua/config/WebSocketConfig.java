package com.ute.trasua.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cấu hình WebSocket STOMP cho thông báo thời gian thực:
 * - Đẩy đơn mới về KDS quầy pha chế Barista (/topic/kitchen/{branchId})
 * - Đẩy cập nhật trạng thái đơn (PREPARING, PREPARED)
 * - Đồng bộ trạng thái thực đơn chi nhánh (Còn/Hết hàng)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-kds")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint("/ws-kds-native")
                .setAllowedOriginPatterns("*");
    }
}
