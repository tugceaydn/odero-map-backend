package com.example.oderomapbackend;

import com.example.oderomapbackend.PaymentData;
import com.example.oderomapbackend.PaymentDataList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000") // Allow CORS for this controller
@RestController
public class PaymentController {
    @Autowired
    private PaymentDataListService paymentDataListService;

    private final PaymentDataList paymentDataList = new PaymentDataList();
    @GetMapping("/api/payment-data")
    public List<PaymentData> getPaymentData() {

        PaymentDataListService service = new PaymentDataListService();
        PaymentDataList paymentDataList = service.addList();
        return paymentDataList.getPaymentDataList();
    }
    @GetMapping("/api/payments/start")
    public String startSendingPayments() {
        paymentDataListService.startSendingPayments(paymentDataList);
        return "Payment data generation started.";
    }

    @GetMapping("/api/payments/current")
    public PaymentDataList getCurrentPayments() {
        return paymentDataList;
    }

}
