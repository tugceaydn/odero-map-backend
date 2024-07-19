package com.example.oderomapbackend;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Data
public class PaymentDataServiceWithQueue {

    private int lastOneHourPaymentSum;
    private int lastDayPaymentSum;
    private int paymentCounterDay;
    private int paymentCounterHour;

    private Queue<PaymentData> queueLastHour;
    private Queue<PaymentData> queueLastDay;
    private final Lock lock = new ReentrantLock();

    public PaymentDataServiceWithQueue() {
        this.lastOneHourPaymentSum = 0;
        this.lastDayPaymentSum = 0;
        this.queueLastDay = new LinkedList<>();
        this.queueLastHour = new LinkedList<>();
        this.paymentCounterDay = 0;
        this.paymentCounterHour = 0;
    }

    public void addPaymentToQueues(PaymentData paymentData) {
        lock.lock();
        try {
            System.out.println("Before adding: "  + queueLastHour.size() + ", " + lastOneHourPaymentSum);
            queueLastDay.add(paymentData);
            queueLastHour.add(paymentData);
            lastDayPaymentSum += paymentData.getAmount();
            lastOneHourPaymentSum += paymentData.getAmount();
            paymentCounterDay++;
            paymentCounterHour++;
            System.out.println("After adding: " + queueLastHour.size() + ", " + lastOneHourPaymentSum);
        } finally {
            lock.unlock();
        }
    }

    public void updateQueues() {

        lock.lock();
        try {
            System.out.println("Before updating: " +  queueLastHour.size() + ", " + (!queueLastHour.isEmpty() ? queueLastHour.peek().getAmount() : 0));
            while (!queueLastDay.isEmpty()) {
                Duration duration = Duration.between(queueLastDay.peek().getTime(), LocalDateTime.now());

                if (duration.abs().toHours() >= 24) {
                    lastDayPaymentSum -= queueLastDay.peek().getAmount();
                    paymentCounterDay--;
                    queueLastDay.remove();
                } else {
                    break;
                }
            }

            while (!queueLastHour.isEmpty()) {
                Duration duration = Duration.between(queueLastHour.peek().getTime(), LocalDateTime.now());
                if (duration.abs().toSeconds() >= 3) {
                    lastOneHourPaymentSum -= queueLastHour.peek().getAmount();
                    paymentCounterHour--;
                    queueLastHour.remove();
                } else {
                    break;
                }
            }

            System.out.println("After updating: " + queueLastHour.size() + ", " + lastOneHourPaymentSum);
        } finally {
            lock.unlock();
        }
    }
}
