package com.revShop.orderservice.service;

import com.revShop.orderservice.dto.OrderResponse;
import com.revShop.orderservice.entity.Order;
import com.revShop.orderservice.entity.OrderStatus;
import com.revShop.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;

    // US-25: every order on the platform, newest first, optionally only one status.
    // Both queries have been in your OrderRepository since Thursday.
    @Transactional(readOnly = true)
    public List<OrderResponse> allOrders(OrderStatus status) {
        List<Order> orders = status == null
                ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findByStatusOrderByCreatedAtDesc(status);
        return orders.stream().map(OrderResponse::from).toList();
    }
}