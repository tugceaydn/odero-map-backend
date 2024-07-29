package com.example.oderomapbackend.config;

import com.example.oderomapbackend.controller.DataWebSocketHandler;
import com.example.oderomapbackend.controller.MerchantWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

//    private final PaymentDataService paymentDataService;


    private final DataWebSocketHandler dataWebSocketHandler;
    private final MerchantWebSocketHandler merchantWebSocketHandler;

//    public WebSocketConfig(PaymentDataService paymentDataService) {
//        this.paymentDataService = paymentDataService;
//    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler( dataWebSocketHandler(paymentDataService), "/data").setAllowedOrigins("*");
//        registry.addHandler( merchantWebSocketHandler(paymentDataService), "/merchantData").setAllowedOrigins("*");

        registry.addHandler(dataWebSocketHandler, "/data").setAllowedOrigins("*");
        registry.addHandler(merchantWebSocketHandler, "/merchantData").setAllowedOrigins("*");
    }
}

