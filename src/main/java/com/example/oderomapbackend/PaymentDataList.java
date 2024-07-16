package com.example.oderomapbackend;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PaymentDataList {
    List<PaymentData> paymentDataList = new ArrayList<>();

    public void setPaymentDataList(PaymentData paymentData) {
        this.paymentDataList.add(paymentData);
    }

    public List<PaymentData> getPaymentDataList() {
        return paymentDataList;
    }
}
