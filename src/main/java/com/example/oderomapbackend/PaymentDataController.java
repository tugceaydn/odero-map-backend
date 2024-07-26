package com.example.oderomapbackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentDataController {

    @Autowired
    private PaymentDataService paymentDataService;
    @Autowired
    private DataWebSocketHandler dataWebSocketHandler;

    @PostMapping
    public void receivePaymentData(@RequestBody PaymentDataRequest paymentDataRequest) {
        System.out.println("payment data request: " + paymentDataRequest);

        // Create PaymentData object from request
        PaymentData paymentData = new PaymentData(
                paymentDataRequest.getAmount(),
                paymentDataRequest.getTimestamp(),
                paymentDataRequest.getCity(),
                paymentDataRequest.getMerchantId(),
                paymentDataRequest.getMerchantName(),
                paymentDataRequest.getMerchantId(),
                paymentDataRequest.getMerchantName(),
                paymentDataRequest.getIp()
        );

//        // Add data to the service
//        paymentDataService.addData(paymentData);

        // Use WebSocket handler to process and send message
        dataWebSocketHandler.getPaymentAndSend(paymentData);
    }
}

