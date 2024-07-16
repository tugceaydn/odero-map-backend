package com.example.oderomapbackend;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import com.example.oderomapbackend.PaymentDataList;
import com.example.oderomapbackend.PaymentData;

public class PaymentDataListService {
    List<String> cities = Arrays.asList("İstanbul", "Ankara", "Karabük", "Samsun", "Malatya", "Mersin", "Elazığ", "Muğla");

    public PaymentDataList addList() {
        PaymentDataList paymentDataList = new PaymentDataList();
        for(int i = 0; i < 100; i++) {
            Random rand = new Random();

            int rand_int = rand.nextInt(1000);

            PaymentData data = new PaymentData(cities.get(i % cities.size()), rand_int);

            paymentDataList.setPaymentDataList(data);
        }

        return paymentDataList;
    }
}
