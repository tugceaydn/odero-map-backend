package com.example.oderomapbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubMerchantData {
    private String name;
    private double amount;
    private int count;
}
