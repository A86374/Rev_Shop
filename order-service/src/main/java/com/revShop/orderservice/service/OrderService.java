package com.revShop.orderservice.service;

import com.revShop.orderservice.client.ProductStockClient;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductStockClient stockClient;

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

    // US-18: a buyer can cancel until the seller ships
    @Transactional
    public OrderResponse cancel(Long id, Long buyerId) {
        Order order = findOwned(id, buyerId);
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING && order.getStatus() != OrderStatus.PLACED) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Order is already " + order.getStatus() + " and can't be cancelled");
        }

        // a PLACED online order has been paid, so the money goes back
        boolean paidOnline = order.getStatus() == OrderStatus.PLACED
                && order.getPaymentMethod() == PaymentMethod.ONLINE;

        order.setStatus(OrderStatus.CANCELLED);
        order.setRefunded(paidOnline);
        releaseStock(order);
        return OrderResponse.from(order);
    }

    // US-15: payment-service reports Pay (success) or Cancel / expiry (failure) for a checkout
    @Transactional
    public int applyPaymentResult(String checkoutRef, boolean success) {
        List<Order> waiting = orderRepository.findByCheckoutRef(checkoutRef).stream()
                .filter(o -> o.getStatus() == OrderStatus.PAYMENT_PENDING)
                .toList();

        for (Order order : waiting) {
            if (success) {
                order.setStatus(OrderStatus.PLACED);
            } else {
                order.setStatus(OrderStatus.CANCELLED);
                releaseStock(order);
            }
        }
        return waiting.size();
    }

    // US-20: product-service asks this before accepting a review
    @Transactional(readOnly = true)
    public boolean hasDelivered(Long buyerId, Long productId) {
        return orderRepository.existsByBuyerIdAndStatusAndItems_ProductId(buyerId, OrderStatus.DELIVERED, productId);
    }

    // someone else's order looks the same as a missing one, so nothing leaks
    Order findOwned(Long id, Long buyerId) {
        return orderRepository.findById(id)
                .filter(o -> o.getBuyerId().equals(buyerId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order " + id + " not found"));
    }

    // the order is already cancelled either way; a failed call is logged so stock can be fixed by hand
    private void releaseStock(Order order) {
        List<ProductStockClient.StockItem> items = order.getItems().stream()
                .map(i -> new ProductStockClient.StockItem(i.getProductId(), i.getQuantity()))
                .toList();
        try {
            stockClient.release(new ProductStockClient.StockRequest(items));
        } catch (Exception e) {
            log.error("Could not put stock back for order {}: {}", order.getId(), e.getMessage());
        }
    }
}