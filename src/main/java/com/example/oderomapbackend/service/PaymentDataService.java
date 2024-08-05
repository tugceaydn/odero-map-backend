package com.example.oderomapbackend.service;

import com.example.oderomapbackend.entity.PaymentData;
import com.example.oderomapbackend.entity.PaymentDataEntry;
import com.example.oderomapbackend.entity.SubMerchantData;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

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

//    private ConcurrentHashMap<String, ConcurrentHashMap<String, DoubleAdder>> merchantTotals = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ConcurrentHashMap<String, SubMerchantData>> merchantTotals = new ConcurrentHashMap<>();

    public PaymentDataService() {
        this.lastOneHourPaymentSum = new DoubleAdder();
        this.lastDayPaymentSum = new DoubleAdder();
        this.paymentCounterHour = new AtomicInteger();
        this.paymentCounterDay = new AtomicInteger();
    }


    public synchronized void addData(PaymentData paymentData) {
        System.out.println("Running adding..."); // For debugging
        int hourSegment = getHourSegment(paymentData.getTimestamp());

        PaymentDataEntry existingDataHour = dataMapHour.get(hourSegment);

        if (existingDataHour != null && (existingDataHour.getTimestamp() / 1000) == (paymentData.getTimestamp() / 1000)) {
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
                    paymentData.getTimestamp(),
                    1
            ));
        }

        lastOneHourPaymentSum.add(paymentData.getAmount());
        lastDayPaymentSum.add(paymentData.getAmount());
        paymentCounterHour.incrementAndGet();
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
            }
        }
        System.out.println("----hour map after delete----");
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
    private int getHourSegment(long timestamp) {
        return (int) ((timestamp / 1000) % HOURSEGMENTS);
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
//    public Map<String, AbstractMap.SimpleEntry<Double, Integer>> getSortedSubMerchantTotals() {
//
//    }
}
