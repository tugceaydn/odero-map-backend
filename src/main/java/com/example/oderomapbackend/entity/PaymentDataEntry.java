package com.example.oderomapbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentDataEntry {
    private double amount;
    private long timestamp;
    private int count;
}
