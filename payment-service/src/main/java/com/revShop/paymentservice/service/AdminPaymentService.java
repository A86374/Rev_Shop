package com.revShop.paymentservice.service;

import com.revShop.paymentservice.dto.AdminPaymentResponse;
import com.revShop.paymentservice.entity.PaymentStatus;
import com.revShop.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPaymentService {

    private final PaymentRepository paymentRepository;

    // US-25: every payment, newest first, optionally only one status.
    // Uses findAll from JpaRepository, so PaymentRepository doesn't change.
    public List<AdminPaymentResponse> allPayments(PaymentStatus status) {
        return paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .filter(p -> status == null || p.getStatus() == status)
                .map(AdminPaymentResponse::from)
                .toList();
    }
}