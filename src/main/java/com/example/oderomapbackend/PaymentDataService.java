package com.example.oderomapbackend;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

@Data
public class PaymentDataService {

    private int lastOneHourPaymentSum;
    private int lastDayPaymentSum;
    private int paymentCounterDay;
    private int paymentCounterHour;

    private Queue<PaymentData> queueLastHour;
    private Queue<PaymentData> queueLastDay;

    public PaymentDataService() {
        this.lastOneHourPaymentSum = 0;
        this.lastDayPaymentSum = 0;
        this.queueLastDay = new LinkedList<>();
        this.queueLastHour = new LinkedList<>();
        this.paymentCounterDay = 0;
        this.paymentCounterHour = 0;
    }

    public void addPaymentToQueues(PaymentData paymentData) {
        queueLastDay.add(paymentData);
        queueLastHour.add(paymentData);
        lastDayPaymentSum += paymentData.getAmount();
        System.out.println("----------" +  lastDayPaymentSum);
        lastOneHourPaymentSum += paymentData.getAmount();
        paymentCounterDay++;
        paymentCounterHour++;
    }

    public void updateQueues(){


        while(!queueLastDay.isEmpty()){
            Duration duration = Duration.between(queueLastDay.peek().getTime(), LocalDateTime.now());

            if(duration.abs().toHours() >= 24){
                lastDayPaymentSum -= queueLastDay.peek().getAmount();
                paymentCounterDay--;
                queueLastDay.remove();

            }
            else{
                break;
            }
        }
        while(!queueLastHour.isEmpty()){
            Duration duration = Duration.between(queueLastHour.peek().getTime(), LocalDateTime.now());
            if(duration.abs().toHours() >= 1){
                lastOneHourPaymentSum -= queueLastHour.peek().getAmount();
                paymentCounterHour--;
                queueLastHour.remove();
            }
            else{
                break;
            }
        }
    }
}
