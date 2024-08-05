package com.example.oderomapbackend.controller;

import com.example.oderomapbackend.service.PaymentDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class MerchantWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private PaymentDataService paymentDataService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

//    public MerchantWebSocketHandler(PaymentDataService paymentDataService) {
//        this.paymentDataService = paymentDataService;
//    }

    public MerchantWebSocketHandler(PaymentDataService paymentDataService) {
        this.paymentDataService = paymentDataService;
//        scheduler.scheduleAtFixedRate(this::sendSortedMerchantData, 0, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        sessions.add(session);
        Map<String, AbstractMap.SimpleEntry<Double, Integer>> sortedMerchantTotals = paymentDataService.getSortedMerchantTotals();

//        printSortedMerchantTotals(sortedMerchantTotals);
        try {
            String jsonMessage = objectMapper.writeValueAsString(sortedMerchantTotals);
            TextMessage textMessage = new TextMessage(jsonMessage);
            for (WebSocketSession mySession : sessions) {
                if (mySession.isOpen()) {
                    mySession.sendMessage(textMessage);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void sendSortedMerchantData() {
        Map<String, AbstractMap.SimpleEntry<Double, Integer>> sortedMerchantTotals = paymentDataService.getSortedMerchantTotals();

//        printSortedMerchantTotals(sortedMerchantTotals);
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
    public void sendSortedSubMerchantData() {

    }
    private void printSortedMerchantTotals(Map<String, Double> sortedMerchantTotals) {
        System.out.println("Sorted Merchant Totals:");
        sortedMerchantTotals.forEach((merchant, total) -> {
            System.out.println("Merchant: " + merchant + ", Total Amount: " + total);
        });
    }
}

