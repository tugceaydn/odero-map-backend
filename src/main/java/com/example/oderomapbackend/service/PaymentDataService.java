package com.example.oderomapbackend.service;

import com.example.oderomapbackend.entity.PaymentData;
import com.example.oderomapbackend.entity.PaymentDataEntry;
import com.example.oderomapbackend.entity.SubMerchantData;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;

@Data
@Service
public class PaymentDataService {

    private DoubleAdder lastOneHourPaymentSum;
    private DoubleAdder lastDayPaymentSum;
    private AtomicInteger paymentCounterHour;
    private AtomicInteger paymentCounterDay;

    private static final int HOURSEGMENTS = 3600;
    private static final int DAYSEGMENTS = 15;
    private ConcurrentHashMap<Integer, PaymentDataEntry> dataMapHour = new ConcurrentHashMap<>(HOURSEGMENTS);
    private ConcurrentHashMap<Integer, PaymentDataEntry> dataMapDay = new ConcurrentHashMap<>(DAYSEGMENTS);

//    private ConcurrentHashMap<String, ConcurrentHashMap<String, DoubleAdder>> merchantTotals = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ConcurrentHashMap<String, SubMerchantData>> merchantTotals = new ConcurrentHashMap<>();
    private boolean dataDeleted = false;
    public PaymentDataService() {
        this.lastOneHourPaymentSum = new DoubleAdder();
        this.lastDayPaymentSum = new DoubleAdder();
        this.paymentCounterHour = new AtomicInteger();
        this.paymentCounterDay = new AtomicInteger();
    }


    public synchronized void addData(PaymentData paymentData) {
        System.out.println("Running adding..."); // For debugging
        int hourSegment = getHourSegment(paymentData.getTimestamp()); // find the segment of the payment
        long milisecondTimestamp = paymentData.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        PaymentDataEntry existingDataHour = dataMapHour.get(hourSegment);
        long currentTime = System.currentTimeMillis();

        if(currentTime - milisecondTimestamp < HOURSEGMENTS * 1000 ){
            System.out.println("TRUEEEEEE");
            if (existingDataHour != null && (existingDataHour.getTimestamp() / 1000) == (milisecondTimestamp / 1000)) {
                dataMapHour.put(hourSegment, new PaymentDataEntry(
                        existingDataHour.getAmount() + paymentData.getAmount(),
                        existingDataHour.getTimestamp(),
                        existingDataHour.getCount() + 1
                ));
            } else {
                if(existingDataHour != null) {
                    lastOneHourPaymentSum.add(-existingDataHour.getAmount());
                    paymentCounterHour.addAndGet(-existingDataHour.getCount());
                }
                dataMapHour.put(hourSegment, new PaymentDataEntry(
                        paymentData.getAmount(),
                        paymentData.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        1
                ));
            }
            lastOneHourPaymentSum.add(paymentData.getAmount());
            paymentCounterHour.incrementAndGet();
        }

        lastDayPaymentSum.add(paymentData.getAmount());

        paymentCounterDay.incrementAndGet();

        String merchantName = paymentData.getMerchantName();
        String merchantId = paymentData.getMerchantId();
        String subMerchantId = paymentData.getSubMerchantId();
        String subMerchantName = paymentData.getSubMerchantName();

        // If subMerchantId is null or empty, use merchantName as the key for subMerchant
        if (subMerchantId == null || subMerchantId.isEmpty()) {
            subMerchantId = merchantId;
            subMerchantName = merchantName;
        }
        final String finalSubMerchantName = subMerchantName;

        // Update merchant and submerchant totals
        merchantTotals.computeIfAbsent(merchantName, k -> new ConcurrentHashMap<>())
                .compute(subMerchantId, (id, subMerchantData) -> {
                    if (subMerchantData == null) {
                        return new SubMerchantData(finalSubMerchantName, paymentData.getAmount(), 1);
                    } else {
                        subMerchantData.setAmount(subMerchantData.getAmount() + paymentData.getAmount());
                        subMerchantData.setCount(subMerchantData.getCount() + 1);
                        return subMerchantData;
                    }
                });

        System.out.println("merchant totals: " + merchantTotals);
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
                dataDeleted = true; // Set the flag to true if data was deleted
            }
        }
        System.out.println("----hour map after delete----");
        System.out.println(dataMapHour);
    }

    public synchronized void dailyCleanUp(){
        System.out.println("lastday payment sum before clean: " + lastDayPaymentSum.sum());
        System.out.println("lastday payment counter before clean: " + paymentCounterDay.get());

        System.out.println("----------------------DAILY CLEANUPPPP----");
        lastDayPaymentSum.reset();
        paymentCounterDay.set(0);

        merchantTotals.clear();

        System.out.println("lastday payment sum after clean: " + lastDayPaymentSum.sum());
        System.out.println("lastday payment counter after clean: " + paymentCounterDay.get());

    }
    private int getHourSegment(LocalDateTime timestamp) {
        long milisTimestamp = timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return (int) ((milisTimestamp / 1000) % HOURSEGMENTS);
    }


    public Map<String, AbstractMap.SimpleEntry<Double, Integer>> getSortedMerchantTotals() {
        return merchantTotals.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            double totalAmount = entry.getValue().values()
                                    .stream()
                                    .mapToDouble(SubMerchantData::getAmount)
                                    .sum();
                            int totalCount = entry.getValue().values()
                                    .stream()
                                    .mapToInt(SubMerchantData::getCount)
                                    .sum();
                            return new AbstractMap.SimpleEntry<>(totalAmount, totalCount);
                        },
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ))
                .entrySet()
                .stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue().getKey(), entry1.getValue().getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    public Map<String, AbstractMap.SimpleEntry<Double, Integer>> getSortedSubMerchantTotals(String merchantName) {
        // Find the submerchants for the given merchant name
        ConcurrentHashMap<String, SubMerchantData> subMerchants = merchantTotals.get(merchantName);

        if (subMerchants == null) {
            // Return an empty map if the merchant does not exist
            return Collections.emptyMap();
        }

        // Collect the submerchant data into a map with submerchant name as the key
        return subMerchants.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getValue().getName(),
                        entry -> new AbstractMap.SimpleEntry<>(entry.getValue().getAmount(), entry.getValue().getCount()),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ))
                .entrySet()
                .stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue().getKey(), entry1.getValue().getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public void resetDataDeleted() {
        dataDeleted = false;
    }
}
