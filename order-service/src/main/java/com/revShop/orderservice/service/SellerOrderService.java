package com.revShop.orderservice.service;

import com.revShop.orderservice.client.SellerPaymentClient;
import com.revShop.orderservice.dto.OrderResponse;
import com.revShop.orderservice.entity.Order;
import com.revShop.orderservice.entity.OrderStatus;
import com.revShop.orderservice.entity.PaymentMethod;
import com.revShop.orderservice.exception.ApiException;
import com.revShop.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerOrderService {

    private final OrderRepository orderRepository;
    private final SellerPaymentClient paymentClient;
    private final OrderNotifier notifier;   // US-21: tells the buyer about shipping and delivery

    // US-19: the seller's orders, newest first, optionally only one status
    @Transactional(readOnly = true)
    public List<OrderResponse> myOrders(Long sellerId, OrderStatus status) {
        List<Order> orders = status == null
                ? orderRepository.findBySellerIdOrderByCreatedAtDesc(sellerId)
                : orderRepository.findBySellerIdAndStatusOrderByCreatedAtDesc(sellerId, status);
        return orders.stream().map(OrderResponse::from).toList();
    }

    // US-19: PLACED -> SHIPPED -> DELIVERED, nothing else
    @Transactional
    public OrderResponse updateStatus(Long orderId, Long sellerId, OrderStatus next) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order " + orderId + " not found"));

        if (!order.getSellerId().equals(sellerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only update your own orders");
        }

        OrderStatus current = order.getStatus();
        boolean allowed = (current == OrderStatus.PLACED && next == OrderStatus.SHIPPED)
                || (current == OrderStatus.SHIPPED && next == OrderStatus.DELIVERED);
        if (!allowed) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot change an order from " + current + " to " + next);
        }

        order.setStatus(next);
        notifier.statusChanged(order);

        if (next == OrderStatus.DELIVERED && order.getPaymentMethod() == PaymentMethod.COD) {
            settleCodIfAllDelivered(order.getCheckoutRef());
        }
        return OrderResponse.from(order);
    }

    // US-16: one COD payment covers every order of a checkout (one per seller),
    // so it is marked paid when the last of them is delivered.
    private void settleCodIfAllDelivered(String checkoutRef) {
        boolean allDone = orderRepository.findByCheckoutRef(checkoutRef).stream()
                .allMatch(o -> o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.CANCELLED);
        if (!allDone) {
            return;
        }
        try {
            paymentClient.settle(new SellerPaymentClient.SettleRequest(checkoutRef));
        } catch (Exception e) {
            // the delivery still counts; the payment can be settled by hand
            log.warn("Could not settle COD payment {}: {}", checkoutRef, e.getMessage());
        }
    }
}