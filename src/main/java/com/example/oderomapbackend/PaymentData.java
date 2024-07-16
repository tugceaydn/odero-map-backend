package com.example.oderomapbackend;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentData {
    private String city;
    private int amount;

}
