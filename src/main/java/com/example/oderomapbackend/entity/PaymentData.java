package com.example.oderomapbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PaymentData {
    private double amount;
//    private long timestamp;
    private LocalDateTime timestamp;
    private String city;
    private String merchantId;
    private String merchantName;
    private String subMerchantId;
    private String subMerchantName;
    private String ip;
}
