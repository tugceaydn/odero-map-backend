package com.example.oderomapbackend.controller;

import com.example.oderomapbackend.service.PaymentDataService;
import com.example.oderomapbackend.entity.PaymentData;
import com.example.oderomapbackend.entity.PaymentDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/payments")
public class PaymentDataController {

    @Autowired
    private PaymentDataService paymentDataService;
    @Autowired
    private DataWebSocketHandler dataWebSocketHandler;
    @Autowired
    private MerchantWebSocketHandler merchantWebSocketHandler;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); // Adjust the pattern according to your timestamp format

    @PostMapping
    public void receivePaymentData(@RequestBody PaymentDataRequest paymentDataRequest) {
        System.out.println("payment data request: " + paymentDataRequest);
        // Convert timestamp string to LocalDateTime
//        LocalDateTime timestamp = LocalDateTime.parse(paymentDataRequest.getTimestamp(), DATE_TIME_FORMATTER);


        // Create PaymentData object from request
        PaymentData paymentData = new PaymentData(
                paymentDataRequest.getAmount(),
                paymentDataRequest.getTimestamp(),
                paymentDataRequest.getCity(),
                paymentDataRequest.getMerchantId(),
                paymentDataRequest.getMerchantName(),
                paymentDataRequest.getSubMerchantId(),
                paymentDataRequest.getSubMerchantName(),
                paymentDataRequest.getIp()
        );

//        // Add data to the service
//        paymentDataService.addData(paymentData);

        // Use WebSocket handler to process and send message
        dataWebSocketHandler.sendMessageWithPayment(paymentData);
        merchantWebSocketHandler.sendSortedMerchantData();
    }
}

