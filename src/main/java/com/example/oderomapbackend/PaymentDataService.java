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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

@Data
@Service
public class PaymentDataService {

    private DoubleAdder lastOneHourPaymentSum;
    private DoubleAdder lastDayPaymentSum;
    private AtomicInteger paymentCounterHour;
    private AtomicInteger paymentCounterDay;

    private static final int HOURSEGMENTS = 5;
    private static final int DAYSEGMENTS = 15;
    private ConcurrentHashMap<Integer, PaymentDataEntry> dataMapHour = new ConcurrentHashMap<>(HOURSEGMENTS);
    private ConcurrentHashMap<Integer, PaymentDataEntry> dataMapDay = new ConcurrentHashMap<>(DAYSEGMENTS);

    public PaymentDataService() {
        this.lastOneHourPaymentSum = new DoubleAdder();
        this.lastDayPaymentSum = new DoubleAdder();
        this.paymentCounterHour = new AtomicInteger();
        this.paymentCounterDay = new AtomicInteger();
    }

    public synchronized void addData(PaymentData paymentData) {
        System.out.println("Running adding..."); // For debugging
        int hourSegment = getHourSegment(paymentData.getTimestamp());
//        int daySegment = getDaySegment(paymentData.getTimestamp());

        PaymentDataEntry existingDataHour = dataMapHour.get(hourSegment);
//        PaymentDataEntry existingDataDay = dataMapDay.get(daySegment);


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
                lastOneHourPaymentSum.add(-existingDataHour.getAmount());

                paymentCounterHour.addAndGet(-existingDataHour.getCount());
            }
            dataMapHour.put(hourSegment, new PaymentDataEntry(
                    paymentData.getAmount(),
                    paymentData.getTimestamp(),
                    1
            ));
        }

        lastOneHourPaymentSum.add(paymentData.getAmount());
        lastDayPaymentSum.add(paymentData.getAmount());
        paymentCounterHour.incrementAndGet();
        paymentCounterDay.incrementAndGet();

//        System.out.println("----hour map after adding----");
//        printMap();

//        System.out.println("----day map after adding----");
//        printMapDay();
    }

    public synchronized void cleanupOldData() {
        long currentTime = System.currentTimeMillis();
        System.out.println("Running cleanupOldData..."); // For debugging

        for (int i = 0; i < HOURSEGMENTS; i++) {


            PaymentDataEntry dataHour = dataMapHour.get(i);
            if (dataHour != null && (currentTime - dataHour.getTimestamp()) > HOURSEGMENTS * 1000) {
                System.out.println("Cleaning up hour segment: " + i + " amount " + dataHour.getAmount()); // For debugging
                lastOneHourPaymentSum.add(-dataHour.getAmount());
                paymentCounterHour.addAndGet(-dataHour.getCount());
                dataMapHour.remove(i);
            }
        }
        System.out.println("----hour map after delete----");
//        printMap();

    }

    public synchronized void dailyCleanUp(){
        System.out.println("lastday payment sum before clean: " + lastDayPaymentSum.sum());
        System.out.println("lastday payment counter before clean: " + paymentCounterDay.get());

        System.out.println("----------------------DAILY CLEANUPPPP----");
        lastDayPaymentSum.reset();
        paymentCounterDay.set(0);

        System.out.println("lastday payment sum after clean: " + lastDayPaymentSum.sum());
        System.out.println("lastday payment counter after clean: " + paymentCounterDay.get());

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
