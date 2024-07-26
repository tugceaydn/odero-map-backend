package com.example.oderomapbackend;

import lombok.Data;

@Data
public class PaymentDataRequest {
    private double amount;
    private long timestamp;
    private String city;
    private String merchantId;
    private String merchantName;
    private String subMerchantId;
    private String subMerchantName;
    private String ip;
}

