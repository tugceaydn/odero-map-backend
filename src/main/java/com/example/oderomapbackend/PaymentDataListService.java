package com.example.oderomapbackend;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.oderomapbackend.PaymentDataList;
import com.example.oderomapbackend.PaymentData;
import org.springframework.stereotype.Service;

@Service
public class PaymentDataListService {
    List<String> cities = Arrays.asList("İstanbul", "Ankara", "Karabük", "Samsun", "Malatya", "Mersin", "Elazığ", "Muğla");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void startSendingPayments(PaymentDataList paymentDataList) {
        scheduler.scheduleAtFixedRate(() -> addPayment(paymentDataList), 0, 1, TimeUnit.SECONDS);
    }

    private void addPayment(PaymentDataList paymentDataList) {
        Random rand = new Random();
        int randInt = rand.nextInt(1000);
        int cityIndex = rand.nextInt(cities.size());

        PaymentData data = new PaymentData(cities.get(cityIndex), randInt);
        paymentDataList.setPaymentDataList(data);

        System.out.println("Added payment: " + data.getCity() + " - " + data.getAmount());
    }


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

//    public PaymentData getPaymentData(int id) {
//        for(int i = 0; i < 100; i++) {
//            Random rand = new Random();
//
//            int rand_int = rand.nextInt(1000);
//            PaymentData data = new PaymentData(cities.get(i % cities.size()), rand_int);
//
//        }
//    }
}
