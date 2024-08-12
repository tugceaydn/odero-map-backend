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
import java.util.*;
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
        System.out.println("Sorted Merchant Data: " + sortedMerchantTotals);
        try {
            List<Map<String, Object>> merchantsData = new ArrayList<>();
            for (String merchant : sortedMerchantTotals.keySet()) {
                Map<String, Object> merchantData = new HashMap<>();
                merchantData.put("merchantName", merchant);
                merchantData.put("totalAmount", sortedMerchantTotals.get(merchant).getKey());
                merchantData.put("totalCount", sortedMerchantTotals.get(merchant).getValue());
                merchantData.put("hasSubMerchants", hasSubMerchants(merchant));
                merchantsData.add(merchantData);
            }

            Map<String, Object> message = new HashMap<>();
            message.put("isSubMerchantData", false);
            message.put("totals", merchantsData);
            // message.put // should say if merchant has submerchants or not
            System.out.println("Sorted Merchant Message: " + message);
            String jsonMessage = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(jsonMessage);
            if (session.isOpen()) {session.sendMessage(textMessage);}

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Received message handleText: " + payload);

        // Assuming the payload is a simple string with the merchant name for simplicity
        // Adjust this logic if you are sending more complex JSON messages
        sendSortedSubMerchantData(payload);
    }
    public void sendSortedMerchantData() {
        Map<String, AbstractMap.SimpleEntry<Double, Integer>> sortedMerchantTotals = paymentDataService.getSortedMerchantTotals();

//        printSortedMerchantTotals(sortedMerchantTotals);
        System.out.println("Sorted Merchant Data: " + sortedMerchantTotals);
        try {
            List<Map<String, Object>> merchantsData = new ArrayList<>();
            for (String merchant : sortedMerchantTotals.keySet()) {
                Map<String, Object> merchantData = new HashMap<>();
                merchantData.put("merchantName", merchant);
                merchantData.put("totalAmount", sortedMerchantTotals.get(merchant).getKey());
                merchantData.put("totalCount", sortedMerchantTotals.get(merchant).getValue());
                merchantData.put("hasSubMerchants", hasSubMerchants(merchant));
                merchantsData.add(merchantData);
            }

            Map<String, Object> message = new HashMap<>();
            message.put("isSubMerchantData", false);
            message.put("totals", merchantsData);
            // message.put // should say if merchant has submerchants or not
            System.out.println("Sorted Merchant Message: " + message);
            String jsonMessage = objectMapper.writeValueAsString(message);
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
    private boolean hasSubMerchants(String merchant) {
        Map<String, AbstractMap.SimpleEntry<Double, Integer>> subMerchantTotals = paymentDataService.getSortedSubMerchantTotals(merchant);
        if (subMerchantTotals.size() == 1) {
            String subMerchantName = subMerchantTotals.keySet().iterator().next();
            return !subMerchantName.equals(merchant);
        }
        return subMerchantTotals.size() > 1;
    }

    public void sendSortedSubMerchantData(String subMerchantId) {
        Map<String, AbstractMap.SimpleEntry<Double, Integer>> sortedSubMerchantTotals = paymentDataService.getSortedSubMerchantTotals(subMerchantId);

//        printSortedMerchantTotals(sortedMerchantTotals);
        System.out.println("Sorted Sub Merchant Data: " + sortedSubMerchantTotals);
        try {
            List<Map<String, Object>> merchantsData = new ArrayList<>();
            for (String merchant : sortedSubMerchantTotals.keySet()) {
                Map<String, Object> merchantData = new HashMap<>();
                merchantData.put("merchantName", merchant);
                merchantData.put("totalAmount", sortedSubMerchantTotals.get(merchant).getKey());
                merchantData.put("totalCount", sortedSubMerchantTotals.get(merchant).getValue());
                merchantData.put("hasSubMerchants", false);
                merchantsData.add(merchantData);
            }
            Map<String, Object> message = new HashMap<>();
            message.put("isSubMerchantData", true);
            message.put("merchant", subMerchantId);
            message.put("totals", merchantsData);

            String jsonMessage = objectMapper.writeValueAsString(message);
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

