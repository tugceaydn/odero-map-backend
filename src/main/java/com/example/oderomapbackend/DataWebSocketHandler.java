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
        }, 0, 2000); // Generate data every 0.2 seconds

        Timer updateTimer = new Timer(true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                paymentDataService.updateQueues();
            }
        }, 0, 60000); // Update queues every minute
    }

    private void generateAndSendData() {
        String city = provinces.get(random.nextInt(provinces.size()));
        int amount = random.nextInt(1000) + 1; // Random amount between 1 and 1000
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        PaymentData paymentData = new PaymentData(city, amount, LocalDateTime.now());
        DataMessage dataMessage = new DataMessage(city,
                                                    amount, timestamp,
                                                    paymentDataService.getLastDayPaymentSum(),
                                                    paymentDataService.getLastOneHourPaymentSum(),
                                                    paymentDataService.getPaymentCounterDay(),
                                                    paymentDataService.getPaymentCounterHour());

        try {
            String jsonMessage = objectMapper.writeValueAsString(dataMessage);
            System.out.println("Sending data: " + jsonMessage); // Print to console
            TextMessage textMessage = new TextMessage(jsonMessage);
            paymentDataService.addPaymentToQueues(paymentData);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                    // Add payment data to queues

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class DataMessage {
        public String city;
        public int amount;
        public String timestamp;
        public int lastDayPaymentSum;
        public int lastOneHourPaymentSum;
        public int paymentCounterDay;
        public int paymentCounterHour;

        public DataMessage(String city, int amount, String timestamp, int lastDayPaymentSum,
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

