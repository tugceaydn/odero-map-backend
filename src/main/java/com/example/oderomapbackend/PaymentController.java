package com.example.oderomapbackend;

import com.example.oderomapbackend.PaymentData;
import com.example.oderomapbackend.PaymentDataList;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000") // Allow CORS for this controller
@RestController
public class PaymentController {

    @GetMapping("/api/payment-data")
    public List<PaymentData> getPaymentData() {

        PaymentDataListService service = new PaymentDataListService();
        PaymentDataList paymentDataList = service.addList();
        return paymentDataList.getPaymentDataList();
    }
}
