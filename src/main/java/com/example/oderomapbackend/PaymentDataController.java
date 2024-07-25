package com.example.oderomapbackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentDataController {

    @Autowired
    private PaymentDataService paymentDataService;

    @PostMapping
    public void receivePaymentData(@RequestBody PaymentDataRequest paymentDataRequest) {
        System.out.println("payment data request: " + paymentDataRequest);

        // Create PaymentData object from request
        PaymentData paymentData = new PaymentData(
                paymentDataRequest.getCity(),
                paymentDataRequest.getMerchantName(),
                paymentDataRequest.getAmount(),
                System.currentTimeMillis() // Use current time as timestamp
        );

        // Add data to the service
        paymentDataService.addData(paymentData);
    }
}

