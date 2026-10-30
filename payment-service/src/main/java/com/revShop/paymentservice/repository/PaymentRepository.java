package com.revShop.paymentservice.repository;

import com.revShop.paymentservice.entity.Payment;
import com.revShop.paymentservice.entity.PaymentMethod;
import com.revShop.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByCheckoutRef(String checkoutRef);

    Optional<Payment> findByCheckoutRef(String checkoutRef);

    List<Payment> findByMethodAndStatusAndExpiresAtBefore(PaymentMethod method, PaymentStatus status,
                                                          LocalDateTime time);
}