package com.revShop.paymentservice.service;

import com.revShop.paymentservice.client.OrderClient;
import com.revShop.paymentservice.dto.CreatePaymentRequest;
import com.revShop.paymentservice.dto.PaymentResponse;
import com.revShop.paymentservice.entity.Payment;
import com.revShop.paymentservice.entity.PaymentAction;
import com.revShop.paymentservice.entity.PaymentMethod;
import com.revShop.paymentservice.entity.PaymentStatus;
import com.revShop.paymentservice.exception.ApiException;
import com.revShop.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    @Value("${app.payment.expiry-minutes}")
    private long expiryMinutes;

    // ONLINE gets an expiry time, COD stays PENDING with no expiry
    @Transactional
    public PaymentResponse create(CreatePaymentRequest req) {
        if (paymentRepository.existsByCheckoutRef(req.checkoutRef())) {
            throw new ApiException(HttpStatus.CONFLICT, "A payment already exists for checkout " + req.checkoutRef());
        }
        Payment payment = Payment.builder()
                .checkoutRef(req.checkoutRef())
                .buyerId(req.buyerId())
                .amount(req.amount())
                .method(req.method())
                .status(PaymentStatus.PENDING)
                .expiresAt(req.method() == PaymentMethod.ONLINE
                        ? LocalDateTime.now().plusMinutes(expiryMinutes) : null)
                .build();
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // Pay -> SUCCESS, Cancel -> FAILED, and the orders follow
    @Transactional(noRollbackFor = ApiException.class)
    public PaymentResponse confirm(Long id, Long userId, PaymentAction action) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment " + id + " not found"));

        if (!payment.getBuyerId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This payment belongs to another user");
        }
        if (payment.getMethod() != PaymentMethod.ONLINE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only online payments use Pay / Cancel");
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Payment is already " + payment.getStatus());
        }
        if (payment.getExpiresAt().isBefore(LocalDateTime.now())) {
            payment.setStatus(PaymentStatus.FAILED);
            notifyOrders(payment);
            throw new ApiException(HttpStatus.CONFLICT, "Payment expired after " + expiryMinutes + " minutes");
        }

        payment.setStatus(action == PaymentAction.PAY ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        notifyOrders(payment);
        return PaymentResponse.from(payment);
    }

    // unpaid ONLINE payments past their expiry -> FAILED, and their orders are cancelled
    @Transactional
    public int expireUnpaid() {
        List<Payment> expired = paymentRepository.findByMethodAndStatusAndExpiresAtBefore(
                PaymentMethod.ONLINE, PaymentStatus.PENDING, LocalDateTime.now());
        expired.forEach(p -> {
            p.setStatus(PaymentStatus.FAILED);
            notifyOrders(p);
        });
        return expired.size();
    }

    // US-16: order-service calls this when the last order of a COD checkout is delivered
    @Transactional
    public PaymentResponse settleCod(String checkoutRef) {
        Payment payment = paymentRepository.findByCheckoutRef(checkoutRef)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No payment for checkout " + checkoutRef));

        if (payment.getMethod() != PaymentMethod.COD) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only cash on delivery payments are settled on delivery");
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Payment is already " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        return PaymentResponse.from(payment);
    }

    // The payment is the source of truth, so if order-service is down we only log it.
    private void notifyOrders(Payment payment) {
        try {
            orderClient.paymentResult(new OrderClient.PaymentResult(
                    payment.getCheckoutRef(), payment.getStatus() == PaymentStatus.SUCCESS));
        } catch (Exception e) {
            log.warn("Could not update the orders of {}: {}", payment.getCheckoutRef(), e.getMessage());
        }
    }
}