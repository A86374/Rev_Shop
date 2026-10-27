package com.revShop.paymentservice.service;

import com.revShop.paymentservice.dto.CreatePaymentRequest;
import com.revShop.paymentservice.dto.PaymentResponse;
import com.revShop.paymentservice.entity.Payment;
import com.revShop.paymentservice.entity.PaymentAction;
import com.revShop.paymentservice.entity.PaymentMethod;
import com.revShop.paymentservice.entity.PaymentStatus;
import com.revShop.paymentservice.exception.ApiException;
import com.revShop.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Value("${app.payment.expiry-minutes}")
    private long expiryMinutes;

    // Task 1 + 3: ONLINE gets a 15-min expiry, COD stays PENDING with no expiry
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

    // Task 1: Pay -> SUCCESS, Cancel -> FAILED
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
            throw new ApiException(HttpStatus.CONFLICT, "Payment expired after " + expiryMinutes + " minutes");
        }

        payment.setStatus(action == PaymentAction.PAY ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        return PaymentResponse.from(payment);
    }

    // Task 2: unpaid ONLINE payments past 15 minutes -> FAILED
    @Transactional
    public int expireUnpaid() {
        List<Payment> expired = paymentRepository.findByMethodAndStatusAndExpiresAtBefore(
                PaymentMethod.ONLINE, PaymentStatus.PENDING, LocalDateTime.now());
        expired.forEach(p -> p.setStatus(PaymentStatus.FAILED));
        return expired.size();
    }
}