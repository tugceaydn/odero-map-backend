package com.example.oderomapbackend;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MerchantWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private PaymentDataService paymentDataService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

//    public MerchantWebSocketHandler(PaymentDataService paymentDataService) {
//        this.paymentDataService = paymentDataService;
//    }

    public MerchantWebSocketHandler(PaymentDataService paymentDataService) {
        this.paymentDataService = paymentDataService;
        scheduler.scheduleAtFixedRate(this::sendSortedMerchantData, 0, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void sendSortedMerchantData() {
        Map<String, Double> sortedMerchantTotals = paymentDataService.getSortedMerchantTotals();

        printSortedMerchantTotals(sortedMerchantTotals);
        try {
            String jsonMessage = objectMapper.writeValueAsString(sortedMerchantTotals);
            TextMessage textMessage = new TextMessage(jsonMessage);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printSortedMerchantTotals(Map<String, Double> sortedMerchantTotals) {
        System.out.println("Sorted Merchant Totals:");
        sortedMerchantTotals.forEach((merchant, total) -> {
            System.out.println("Merchant: " + merchant + ", Total Amount: " + total);
        });
    }
}

