package com.revShop.paymentservice.scheduler;

import com.revShop.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpiryJob {

    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 60_000)   // every minute
    public void expireUnpaidPayments() {
        int count = paymentService.expireUnpaid();
        if (count > 0) {
            log.info("Expired {} unpaid online payment(s)", count);
        }
    }
}