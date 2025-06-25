package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的内存消息代理
        config.enableSimpleBroker("/api/topic"); 
        // 客户端订阅的前缀
        config.setApplicationDestinationPrefixes("/api/app"); 
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket端点，允许跨域
        registry.addEndpoint("/api/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
