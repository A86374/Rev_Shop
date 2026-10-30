package com.revShop.orderservice.service;

import com.revShop.orderservice.dto.OrderResponse;
import com.revShop.orderservice.entity.Order;
import com.revShop.orderservice.exception.ApiException;
import com.revShop.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    // US-17: newest first
    @Transactional(readOnly = true)
    public List<OrderResponse> myOrders(Long buyerId) {
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse myOrder(Long id, Long buyerId) {
        return OrderResponse.from(findOwned(id, buyerId));
    }

    // someone else's order looks the same as a missing one, so nothing leaks
    Order findOwned(Long id, Long buyerId) {
        return orderRepository.findById(id)
                .filter(o -> o.getBuyerId().equals(buyerId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order " + id + " not found"));
    }
}