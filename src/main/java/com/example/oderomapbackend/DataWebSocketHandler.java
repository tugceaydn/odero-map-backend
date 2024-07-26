package com.example.oderomapbackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
@Component
public class DataWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    private final PaymentDataService paymentDataService;


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

    private final List<String> merchants = List.of(
            "Arçelik", "Koç", "Beko", "Vestel", "Siemens", "Bosch"
    );

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public DataWebSocketHandler(PaymentDataService paymentDataService) {

        this.paymentDataService = paymentDataService;
//        scheduler.scheduleAtFixedRate(this::generateAndSendData, 0, 200, TimeUnit.MILLISECONDS);
//        scheduler.scheduleAtFixedRate(paymentDataService::cleanupOldData, 0, 5, TimeUnit.SECONDS);
//        scheduler.scheduleAtFixedRate(paymentDataService::dailyCleanUp, 0, 10 * 1000, TimeUnit.MILLISECONDS); // 24 * 60 * 60 * 1000  //initaildelay: getInitialDelay olmalı
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
//        // Calculate delay until midnight
//        long currentMillis = System.currentTimeMillis();
//        long midnightMillis = java.time.LocalDateTime.now()
//                .toLocalDate()
//                .atStartOfDay()
//                .plusDays(1)
//                .atZone(java.time.ZoneId.systemDefault())
//                .toInstant()
//                .toEpochMilli();
//        return midnightMillis - currentMillis;
//    }
public void getPaymentAndSend(PaymentData paymentData) {
    // Add data to the service
    paymentDataService.addData(paymentData);

    try {
        // Create a DataMessage object
        DataMessage dataMessage = new DataMessage(
                paymentData.getAmount(),
                paymentData.getTimestamp(),
                paymentData.getCity(),
                paymentData.getMerchantId(),
                paymentData.getMerchantName(),
                paymentData.getSubMerchantId(),
                paymentData.getSubMerchantName(),
                paymentData.getIp(),
                paymentDataService.getLastDayPaymentSum().sum(),
                paymentDataService.getLastOneHourPaymentSum().sum(),
                paymentDataService.getPaymentCounterDay().get(),
                paymentDataService.getPaymentCounterHour().get()
        );

        // Convert DataMessage to JSON
        String jsonMessage = objectMapper.writeValueAsString(dataMessage);
        TextMessage textMessage = new TextMessage(jsonMessage);

        // Send message to all connected clients
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(textMessage);
            }
        }
        System.out.println("Counter Day : " + paymentDataService.getPaymentCounterDay());
    } catch (IOException e) {
        e.printStackTrace();
    }
}

//    private void generateAndSendData() {
//        synchronized (paymentDataService) {
//            try {
//                String city = provinces.get(random.nextInt(provinces.size()));
//                String merchantName = merchants.get(random.nextInt(merchants.size()));
//                String merchantId = "M-" + random.nextInt(1000); // Generating a random merchant ID
//                String subMerchantName = "Sub-" + random.nextInt(1000); // Generating a random sub-merchant name
//                String subMerchantId = "SM-" + random.nextInt(1000); // Generating a random sub-merchant ID
//                String ip = "192.168." + random.nextInt(256) + "." + random.nextInt(256); // Generating a random IP
//                double amount = random.nextDouble() * 1000; // Random amount between 0 and 1000
//                long timestamp = System.currentTimeMillis();
//
//                PaymentData paymentData1 = new PaymentData(amount, timestamp, city, merchantId, merchantName, subMerchantId, subMerchantName, ip);
//                paymentDataService.addData(paymentData1);
//
//                DataMessage dataMessage1 = new DataMessage(city, amount, timestamp,
//                        paymentDataService.getLastDayPaymentSum().sum(),
//                        paymentDataService.getLastOneHourPaymentSum().sum(),
//                        paymentDataService.getPaymentCounterDay().get(),
//                        paymentDataService.getPaymentCounterHour().get());
//
//                PaymentData paymentData2 = new PaymentData(amount + 100, timestamp, city, merchantId, merchantName, subMerchantId, subMerchantName, ip);
//                paymentDataService.addData(paymentData2);
//
//                DataMessage dataMessage2 = new DataMessage(city, amount + 100, timestamp,
//                        paymentDataService.getLastDayPaymentSum().sum(),
//                        paymentDataService.getLastOneHourPaymentSum().sum(),
//                        paymentDataService.getPaymentCounterDay().get(),
//                        paymentDataService.getPaymentCounterHour().get());
//
//                String jsonMessage1 = objectMapper.writeValueAsString(dataMessage1);
//                System.out.println("Sending data: " + jsonMessage1); // Print to console
//                TextMessage textMessage1 = new TextMessage(jsonMessage1);
//
//                String jsonMessage2 = objectMapper.writeValueAsString(dataMessage2);
//                System.out.println("Sending data: " + jsonMessage2); // Print to console
//                TextMessage textMessage2 = new TextMessage(jsonMessage2);
//
//                for (WebSocketSession session : sessions) {
//                    if (session.isOpen()) {
//                        session.sendMessage(textMessage1);
//                        // TimeUnit.SECONDS.sleep(1);
//                        session.sendMessage(textMessage2);
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public class DataMessage {
        public double amount;
        public long timestamp;
        public String city;
        public String merchantId;
        public String merchantName;
        public String subMerchantId;
        public String subMerchantName;
        public String ip;
        public double lastDayPaymentSum;
        public double lastOneHourPaymentSum;
        public int paymentCounterDay;
        public int paymentCounterHour;

        public DataMessage(double amount, long timestamp, String city, String merchantId, String merchantName,
                           String subMerchantId, String subMerchantName, String ip, double lastDayPaymentSum,
                           double lastOneHourPaymentSum, int paymentCounterDay, int paymentCounterHour) {
            this.amount = amount;
            this.timestamp = timestamp;
            this.city = city;
            this.merchantId = merchantId;
            this.merchantName = merchantName;
            this.subMerchantId = subMerchantId;
            this.subMerchantName = subMerchantName;
            this.ip = ip;
            this.lastDayPaymentSum = lastDayPaymentSum;
            this.lastOneHourPaymentSum = lastOneHourPaymentSum;
            this.paymentCounterDay = paymentCounterDay;
            this.paymentCounterHour = paymentCounterHour;
        }
    }

}
