// DataWebSocketHandler.java
package com.example.oderomapbackend;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class DataWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    private PaymentDataService paymentDataService = new PaymentDataService();

    private final List<String> provinces = List.of(
            "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Amasya", "Ankara", "Antalya", "Artvin", "Aydın", "Balıkesir",
            "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa", "Çanakkale", "Çankırı", "Çorum", "Denizli",
            "Diyarbakır", "Edirne", "Elazığ", "Erzincan", "Erzurum", "Eskişehir", "Gaziantep", "Giresun", "Gümüşhane",
            "Hakkâri", "Hatay", "Isparta", "Mersin", "İstanbul", "İzmir", "Kars", "Kastamonu", "Kayseri", "Kırklareli",
            "Kırşehir", "Kocaeli", "Konya", "Kütahya", "Malatya", "Manisa", "Kahramanmaraş", "Mardin", "Muğla", "Muş",
            "Nevşehir", "Niğde", "Ordu", "Rize", "Sakarya", "Samsun", "Siirt", "Sinop", "Sivas", "Tekirdağ", "Tokat",
            "Trabzon", "Tunceli", "Şanlıurfa", "Uşak", "Van", "Yozgat", "Zonguldak", "Aksaray", "Bayburt", "Karaman",
            "Kırıkkale", "Batman", "Şırnak", "Bartın", "Ardahan", "Iğdır", "Yalova", "Karabük", "Kilis", "Osmaniye",
            "Düzce"
    );

//    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

//    public DataWebSocketHandler() {
//        Timer timer = new Timer(true);
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                generateAndSendData();
//            }
//        }, 0, 200); // Generate data every 1 second
//
//        Timer updateTimer = new Timer(true);
//        updateTimer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                paymentDataService.cleanupOldData();
//            }
//        }, 0, 5000); // Update queues every 5 seconds
//
//        Timer dailyTimer = new Timer(true);
//        dailyTimer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                paymentDataService.dailyCleanUp();
//                System.out.println("Initial Delay: " + getInitialDelay()); ;
//            }
//        }, 0,   10 * 1000); // 24 hours in milliseconds  24 * 60 * 60 * 1000
//    }

    public DataWebSocketHandler() {

        scheduler.scheduleAtFixedRate(this::generateAndSendData, 0, 200, TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(paymentDataService::cleanupOldData, 0, 5, TimeUnit.SECONDS);

        scheduler.scheduleAtFixedRate(paymentDataService::dailyCleanUp, getInitialDelay(), 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);

    }
    private long getInitialDelay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date midnight = calendar.getTime();
        long currentTime = System.currentTimeMillis();

        if (midnight.getTime() < currentTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            midnight = calendar.getTime();
        }

        return midnight.getTime() - currentTime;
    }

//    private long getInitialDelay() {
//
//        // Calculate delay until midnight
//
//        long currentMillis = System.currentTimeMillis();
//
//        long midnightMillis = java.time.LocalDateTime.now()
//
//                .toLocalDate()
//
//                .atStartOfDay()
//
//                .plusDays(1)
//
//                .atZone(java.time.ZoneId.systemDefault())
//
//                .toInstant()
//
//                .toEpochMilli();
//
//        return midnightMillis - currentMillis;
//
//    }

    private void generateAndSendData() {
        String city = provinces.get(random.nextInt(provinces.size()));
        double amount = random.nextDouble() * 1000; // Random amount between 0 and 1000
        long timestamp = System.currentTimeMillis();

        PaymentData paymentData1 = new PaymentData(city, amount, timestamp);

        paymentDataService.addData(paymentData1);

        DataMessage dataMessage1 = new DataMessage(city, amount, timestamp,

                paymentDataService.getLastDayPaymentSum().sum(),
                paymentDataService.getLastOneHourPaymentSum().sum(),
                paymentDataService.getPaymentCounterDay().get(),
                paymentDataService.getPaymentCounterHour().get());


        PaymentData paymentData2 = new PaymentData(city, (amount + 100), timestamp);
        paymentDataService.addData(paymentData2);

        DataMessage dataMessage2 = new DataMessage(city, amount + 100, timestamp,
                paymentDataService.getLastDayPaymentSum().sum(),
                paymentDataService.getLastOneHourPaymentSum().sum(),
                paymentDataService.getPaymentCounterDay().get(),
                paymentDataService.getPaymentCounterHour().get());

        try {
            String jsonMessage1 = objectMapper.writeValueAsString(dataMessage1);
            System.out.println("Sending data: " + jsonMessage1); // Print to console
            TextMessage textMessage1 = new TextMessage(jsonMessage1);

            String jsonMessage2 = objectMapper.writeValueAsString(dataMessage2);
            System.out.println("Sending data: " + jsonMessage2); // Print to console
            TextMessage textMessage2 = new TextMessage(jsonMessage2);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage1);
//                    TimeUnit.SECONDS.sleep(1);
                    session.sendMessage(textMessage2);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    private static class DataMessage {
        public String city;
        public double amount;
        public long timestamp;
        public double lastDayPaymentSum;
        public double lastOneHourPaymentSum;
        public int paymentCounterDay;
        public int paymentCounterHour;

        public DataMessage(String city, double amount, long timestamp, double lastDayPaymentSum,
                           double lastOneHourPaymentSum, int paymentCounterDay, int paymentCounterHour) {
            this.city = city;
            this.amount = amount;
            this.timestamp = timestamp;
            this.lastDayPaymentSum = lastDayPaymentSum;
            this.lastOneHourPaymentSum = lastOneHourPaymentSum;
            this.paymentCounterDay = paymentCounterDay;
            this.paymentCounterHour = paymentCounterHour;
        }
    }
}
