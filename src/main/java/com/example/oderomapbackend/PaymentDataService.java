package com.example.oderomapbackend;

import org.apache.commons.lang3.tuple.Pair;
import lombok.Data;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Data
@Service
public class PaymentDataService {

    private int lastOneHourPaymentSum;
    private int lastDayPaymentSum;
    private int paymentCounterHour;
    private int paymentCounterDay;

    private static final int HOURSEGMENTS = 5;
    private static final int DAYSEGMENTS = 15;
    private ConcurrentHashMap<Integer, PaymentDataEntry> dataMapHour = new ConcurrentHashMap<>(HOURSEGMENTS);
    private ConcurrentHashMap<Integer, PaymentDataEntry> dataMapDay = new ConcurrentHashMap<>(DAYSEGMENTS);

    public PaymentDataService() {
        this.lastOneHourPaymentSum = 0;
        this.lastDayPaymentSum = 0;
        this.paymentCounterHour = 0;
        this.paymentCounterDay = 0;
    }

    public void addData(PaymentData paymentData) {
        System.out.println("Running adding..."); // For debugging
        int hourSegment = getHourSegment(paymentData.getTimestamp());
        int daySegment = getDaySegment(paymentData.getTimestamp());

        PaymentDataEntry existingDataHour = dataMapHour.get(hourSegment);
        PaymentDataEntry existingDataDay = dataMapDay.get(daySegment);


        if (existingDataHour != null && (existingDataHour.getTimestamp() / 1000) == (paymentData.getTimestamp() / 1000)) {
            // Same second, update amount and count
//            System.out.println("ustune ekle hour " + paymentData.getAmount());
            dataMapHour.put(hourSegment, new PaymentDataEntry(
                    existingDataHour.getAmount() + paymentData.getAmount(),
                    existingDataHour.getTimestamp(),
                    existingDataHour.getCount() + 1
            ));
        } else {
            // Different second, replace with new data
//            System.out.println("ustune yaz hour " + paymentData.getAmount());
            if(existingDataHour != null) {
                lastOneHourPaymentSum -= existingDataHour.getAmount();
                paymentCounterHour -= existingDataHour.getCount();
            }
            dataMapHour.put(hourSegment, new PaymentDataEntry(
                    paymentData.getAmount(),
                    paymentData.getTimestamp(),
                    1
            ));
        }

        if (existingDataDay != null && (existingDataDay.getTimestamp() / 1000) == (paymentData.getTimestamp() / 1000)) {
            // Same second, update amount and count
//            System.out.println("ustune ekle day " + paymentData.getAmount());
            dataMapDay.put(daySegment, new PaymentDataEntry(
                    existingDataDay.getAmount() + paymentData.getAmount(),
                    existingDataDay.getTimestamp(),
                    existingDataDay.getCount() + 1
            ));
        } else {
            // Different second, replace with new data
//            System.out.println("ustune yaz day " + paymentData.getAmount());
            if(existingDataDay != null) {
                lastDayPaymentSum -= existingDataDay.getAmount();
//                paymentCounterDay--;
                setPaymentCounterDay(getPaymentCounterDay() - 1);
            }
            dataMapDay.put(daySegment, new PaymentDataEntry(
                    paymentData.getAmount(),
                    paymentData.getTimestamp(),
                    1
            ));
        }

        lastOneHourPaymentSum += paymentData.getAmount();
        lastDayPaymentSum += paymentData.getAmount();
        paymentCounterHour++;
//        paymentCounterDay++;
        setPaymentCounterDay(getPaymentCounterDay() + 1);

//        System.out.println("----hour map after adding----");
//        printMap();

//        System.out.println("----day map after adding----");
//        printMapDay();
    }

    public void cleanupOldData() {
        long currentTime = System.currentTimeMillis();
        System.out.println("Running cleanupOldData..."); // For debugging

        for (int i = 0; i < HOURSEGMENTS; i++) {


            PaymentDataEntry dataHour = dataMapHour.get(i);
            if (dataHour != null && (currentTime - dataHour.getTimestamp()) > HOURSEGMENTS * 1000) {
                System.out.println("Cleaning up hour segment: " + i + " amount " + dataHour.getAmount()); // For debugging
                lastOneHourPaymentSum -= dataHour.getAmount();
                paymentCounterHour -= dataHour.getCount();
                dataMapHour.remove(i);
            }
        }
        System.out.println("----hour map after delete----");
//        printMap();

    }

    public void dailyCleanUp(){
        System.out.println("lastday payment sum before clean: " + getLastDayPaymentSum());
        System.out.println("lastday payment counter before clean: " + getPaymentCounterDay());

        System.out.println("----------------------DAILY CLEANUPPPP----");
        setLastDayPaymentSum(0);
        setPaymentCounterDay(0);

        System.out.println("lastday payment sum after clean: " + getLastDayPaymentSum());
        System.out.println("lastday payment counter after clean: " + getPaymentCounterDay());
//
//        printMapDay();
    }
    private int getHourSegment(long timestamp) {
        return (int) ((timestamp / 1000) % HOURSEGMENTS);
    }

    private int getDaySegment(long timestamp) {
        return (int) ((timestamp / 1000) % DAYSEGMENTS);
    }

    private boolean isSameDay(long timestamp, long currentTime) {
        long entryDate = TimeUnit.MILLISECONDS.toDays(timestamp);
        long currentDate = TimeUnit.MILLISECONDS.toDays(currentTime);
        System.out.println("entry date " + entryDate + " current date " + currentDate);

        return entryDate == currentDate;
    }

    public void printMap() {
        dataMapHour.forEach((k, v) -> {
            if (v != null) {
                System.out.println("Segment " + k + ": " + v.getAmount() + " at " + v.getTimestamp() + " count: " + v.getCount());
            }
        });
    }

    public void printMapDay() {
        dataMapDay.forEach((k, v) -> {
            if (v != null) {
                System.out.println("Segment " + k + ": " + v.getAmount() + " at " + v.getTimestamp() + " count: " + v.getCount());
            }
        });
    }
}
