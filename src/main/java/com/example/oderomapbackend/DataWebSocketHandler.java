// DataWebSocketHandler.java
package com.example.oderomapbackend;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public DataWebSocketHandler() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                generateAndSendData();
            }
        }, 0, 200); // Generate data every 1 second

        Timer updateTimer = new Timer(true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                paymentDataService.cleanupOldData();
            }
        }, 0, 5000); // Update queues every 3 seconds
    }

    private void generateAndSendData() {
        String city = provinces.get(random.nextInt(provinces.size()));
        int amount = random.nextInt(1000) + 1; // Random amount between 1 and 1000
        long timestamp = System.currentTimeMillis();

        PaymentData paymentData1 = new PaymentData(city, amount, timestamp);

        paymentDataService.addData(paymentData1);

        DataMessage dataMessage1 = new DataMessage(city, amount, timestamp,
                paymentDataService.getLastDayPaymentSum(),
                paymentDataService.getLastOneHourPaymentSum(),
                paymentDataService.getPaymentCounterDay(),
                paymentDataService.getPaymentCounterHour());


        PaymentData paymentData2 = new PaymentData(city, (amount + 100), timestamp);
        paymentDataService.addData(paymentData2);

        DataMessage dataMessage2 = new DataMessage(city, amount + 100, timestamp,
                paymentDataService.getLastDayPaymentSum(),
                paymentDataService.getLastOneHourPaymentSum(),
                paymentDataService.getPaymentCounterDay(),
                paymentDataService.getPaymentCounterHour());

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
        public int amount;
        public long timestamp;
        public int lastDayPaymentSum;
        public int lastOneHourPaymentSum;
        public int paymentCounterDay;
        public int paymentCounterHour;

        public DataMessage(String city, int amount, long timestamp, int lastDayPaymentSum,
                           int lastOneHourPaymentSum, int paymentCounterDay, int paymentCounterHour) {
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
