package com.example.oderomapbackend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PaymentDataService paymentDataService;

    public WebSocketConfig(PaymentDataService paymentDataService) {
        this.paymentDataService = paymentDataService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new DataWebSocketHandler(paymentDataService), "/data").setAllowedOrigins("*");
        registry.addHandler(new MerchantWebSocketHandler(paymentDataService), "/merchantData").setAllowedOrigins("*");
    }
}

