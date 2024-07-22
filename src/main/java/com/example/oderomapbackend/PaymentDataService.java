package com.example.oderomapbackend;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PaymentDataService {    private static final int SEGMENTSHOUR = 10; private static final int SEGMENTSDAY = 86400;
    private ConcurrentHashMap<Integer, CityData> dataMapHour = new ConcurrentHashMap<>(SEGMENTSHOUR);
    private ConcurrentHashMap<Integer, CityData> dataMapDay = new ConcurrentHashMap<>(SEGMENTSDAY);

    public PaymentDataService() {

    }

    public void addData(int amount, long timestamp) {
        int segmentHour = getSegment(timestamp);
        CityData existingDataHour = dataMapHour.get(segmentHour);
        System.out.println("Adding segmentHour " + segmentHour + " data: " + existingDataHour);
        if (existingDataHour != null && (existingDataHour.getTimestamp() / 1000) == (timestamp / 1000)) {
            // Same second, update amount
            existingDataHour.setAmount(existingDataHour.getAmount() + amount);
        } else {
            // Different second, replace with new data
            dataMapHour.put(segmentHour, new CityData(amount, timestamp));
        }
        int segmentDay = getSegment(timestamp);
        CityData existingDataDay = dataMapDay.get(segmentDay);
//        System.out.println("Adding segmentDay" + segmentDay + "data: " + existingDataDay);
        if (existingDataDay != null && (existingDataDay.getTimestamp() / 1000) == (timestamp / 1000)) {
            // Same second, update amount
            existingDataDay.setAmount(existingDataDay.getAmount() + amount);
        } else {
            // Different second, replace with new data
            dataMapDay.put(segmentDay, new CityData(amount, timestamp));
        }
    }

    //@Scheduled(fixedRate = 60000) // Runs every minute
    public void cleanupOldDataHour() {
        long currentTime = System.currentTimeMillis();
        System.out.println("-------Before Cleaning :");
        printMap(dataMapHour);
        System.out.println("Cleaning up Old DataHour----");
        for (int i = 0; i < SEGMENTSHOUR; i++) {
            System.out.println("SegmentHour" + i + ": " + dataMapHour.get(i));
            CityData data = dataMapHour.get(i);
            if (data != null && (currentTime - data.getTimestamp()) > SEGMENTSHOUR * 1000) {
                dataMapHour.remove(i);
            }
        }
        System.out.println("After Cleaning :");
        printMap(dataMapHour);
        for (int i = 0; i < SEGMENTSDAY; i++) {
            CityData data = dataMapDay.get(i);
            if (data != null && (currentTime - data.getTimestamp()) > SEGMENTSDAY * 1000) {
                dataMapDay.remove(i);
            }
        }
    }


    private int getSegment(long timestamp) {
        return (int) ((timestamp / 1000) % SEGMENTSHOUR);
    }

    public void printMap(ConcurrentHashMap<Integer, CityData> dataMap) {
        dataMap.forEach((k, v) -> {
            if (v != null) {
                System.out.println("Key " + k + ": " + v.getAmount() + " at " + v.getTimestamp());
            }
        });
    }

    public int getPaymentCounterDay(){
        return dataMapDay.size();
    }
    public int getPaymentCounterHour(){
        return dataMapHour.size();
    }
    public int getLastDayPaymentSum(){
        AtomicInteger sum = new AtomicInteger();
        dataMapDay.forEach((k, v) -> {
            if (v != null) {
                sum.addAndGet(v.getAmount());
            }
        });
        return sum.get();
    }
    public int getLastHourPaymentSum(){
        AtomicInteger sum = new AtomicInteger();
        dataMapHour.forEach((k, v) -> {
            if (v != null) {
                sum.addAndGet(v.getAmount());
            }
        });
        return sum.get();
    }
}
