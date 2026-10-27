package com.revShop.paymentservice.repository;

import com.revShop.paymentservice.entity.Payment;
import com.revShop.paymentservice.entity.PaymentMethod;
import com.revShop.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByCheckoutRef(String checkoutRef);

    List<Payment> findByMethodAndStatusAndExpiresAtBefore(PaymentMethod method, PaymentStatus status,
                                                          LocalDateTime time);
}