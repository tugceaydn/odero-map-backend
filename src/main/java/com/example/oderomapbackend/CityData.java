package com.example.oderomapbackend;

import lombok.Data;

@Data
public class CityData {
    private int amount;
    private long timestamp; // In milliseconds

    public CityData(int amount, long timestamp) {
        this.amount = amount;
        this.timestamp = timestamp;
    }

}
