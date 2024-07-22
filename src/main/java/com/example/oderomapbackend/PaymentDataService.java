package com.example.oderomapbackend;

//import ch.qos.logback.core.joran.sanity.Pair;
import org.apache.commons.lang3.tuple.Pair;
import lombok.Data;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Service
public class PaymentDataService {

    private int lastOneHourPaymentSum;
    private int lastDayPaymentSum;
    private int paymentCounterHour;
    private int paymentCounterDay;

    private static final int HOURSEGMENTS = 5;
    private static final int DAYSEGMENTS = 15;
    private ConcurrentHashMap<Integer, Pair<Integer, Long>> dataMapHour = new ConcurrentHashMap<>(HOURSEGMENTS);
    private ConcurrentHashMap<Integer, Pair<Integer, Long>> dataMapDay = new ConcurrentHashMap<>(DAYSEGMENTS);

    public PaymentDataService() {
        this.lastOneHourPaymentSum = 0;
        this.lastDayPaymentSum = 0;
        this.paymentCounterHour = 0;
        this.paymentCounterDay = 0;
    }

    public void addData(PaymentData paymentData) {
        int hourSegment = getHourSegment(paymentData.getTimestamp());
        int daySegment = getDaySegment(paymentData.getTimestamp());

        Pair<Integer, Long> existingDataHour = dataMapHour.get(hourSegment);
        Pair<Integer, Long> existingDataDay = dataMapDay.get(daySegment);


        if (existingDataHour != null && (existingDataHour.getRight() / 1000) == (paymentData.getTimestamp() / 1000)) {
            // Same second, update amount
            System.out.println("ustune ekle hour" + paymentData.getAmount());
            dataMapHour.put(hourSegment, Pair.of(existingDataHour.getLeft() + paymentData.getAmount(), existingDataHour.getRight()));
        } else {
            // Different second, replace with new data
            System.out.println("ustune yaz hour" + paymentData.getAmount());
            dataMapHour.put(hourSegment, Pair.of(paymentData.getAmount(), paymentData.getTimestamp()));
        }

        if (existingDataDay != null && (existingDataDay.getRight() / 1000) == (paymentData.getTimestamp() / 1000)) {
            // Same second, update amount
            System.out.println("ustune ekle day" + paymentData.getAmount());
            dataMapDay.put(daySegment, Pair.of(existingDataDay.getLeft() + paymentData.getAmount(), existingDataDay.getRight()));
        } else {
            // Different second, replace with new data
            System.out.println("ustune yaz day" + paymentData.getAmount());
            dataMapDay.put(daySegment, Pair.of(paymentData.getAmount(), paymentData.getTimestamp()));
        }

        lastOneHourPaymentSum += paymentData.getAmount();
        lastDayPaymentSum += paymentData.getAmount();
        paymentCounterDay++;
        paymentCounterHour++;

        System.out.println("----hour map after adding----");
        printMap();

        System.out.println("----day map after adding----");
        printMapDay();
    }

    public void cleanupOldData() {
        long currentTime = System.currentTimeMillis();
        System.out.println("Running cleanupOldData..."); // For debugging

        for (int i = 0; i < DAYSEGMENTS; i++) {

            if(i < HOURSEGMENTS) {
                Pair<Integer, Long> dataHour = dataMapHour.get(i);
                System.out.println("hour Segment " + i + ": " + dataHour);
                if (dataHour != null && (currentTime - dataHour.getRight()) > HOURSEGMENTS * 1000) {
                    System.out.println("Cleaning up hour segment: " + i); // For debugging
                    lastOneHourPaymentSum -= dataHour.getLeft();
                    dataMapHour.remove(i);
                    paymentCounterHour--;
                }
            }

            Pair<Integer, Long> dataDay = dataMapDay.get(i);
            System.out.println("day Segment " + i + ": " + dataDay); // For debugging

            if (dataDay != null && (currentTime - dataDay.getRight()) > DAYSEGMENTS * 1000) {
                System.out.println("Cleaning up day segment: " + i); // For debugging
                lastDayPaymentSum -= dataDay.getLeft();
                dataMapDay.remove(i);
                paymentCounterDay--;
            }
        }

        System.out.println("----hour map after delete----");
        printMap();

        System.out.println("----day map after delete----");
        printMapDay();
    }

    private int getHourSegment(long timestamp) {
        return (int) ((timestamp / 1000) % HOURSEGMENTS);
    }

    private int getDaySegment(long timestamp) {
        return (int) ((timestamp / 1000) % DAYSEGMENTS);
    }

    public void printMap() {
        dataMapHour.forEach((k, v) -> {
            if (v != null) {
                System.out.println("Segment " + k + ": " + v.getKey() + " at " + v.getValue());
            }
        });
    }
    public void printMapDay() {
        dataMapDay.forEach((k, v) -> {
            if (v != null) {
                System.out.println("Segment " + k + ": " + v.getKey() + " at " + v.getValue());
            }
        });
    }
}
