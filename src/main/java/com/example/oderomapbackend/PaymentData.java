package com.example.oderomapbackend;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PaymentData {
    private String city;
    private String merchant;
    private double amount;
    private long timestamp;
}
