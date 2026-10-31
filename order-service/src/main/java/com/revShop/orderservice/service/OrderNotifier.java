package com.revShop.orderservice.service;

import com.revShop.orderservice.client.NotificationClient;
import com.revShop.orderservice.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

// US-21: turns order events into bell notifications.
// A failed notification never breaks the order: it is only logged.
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotifier {

    private final NotificationClient client;

    // a COD checkout (Nikitha) or an online payment that went through (you)
    public void placed(List<Order> orders) {
        if (orders.isEmpty()) {
            return;
        }
        Order first = orders.get(0);
        send(first.getBuyerId(), "ORDER_PLACED",
                "Your order " + first.getCheckoutRef() + " is placed. We will tell you when it ships.");
        for (Order order : orders) {
            send(order.getSellerId(), "ORDER_PLACED",
                    "New order #" + order.getId() + " (₹" + order.getTotalAmount() + ") is waiting to be shipped.");
        }
    }

    // the seller marks an order SHIPPED or DELIVERED (Hasitha)
    public void statusChanged(Order order) {
        String text = switch (order.getStatus()) {
            case SHIPPED -> "Your order #" + order.getId() + " has been shipped.";
            case DELIVERED -> "Your order #" + order.getId() + " has been delivered. You can now review its products.";
            default -> null;
        };
        if (text != null) {
            send(order.getBuyerId(), "ORDER_STATUS", text);
        }
    }

    // the buyer cancels (you)
    public void cancelled(Order order) {
        send(order.getSellerId(), "ORDER_STATUS",
                "Order #" + order.getId() + " was cancelled by the buyer. Don't ship it.");
        if (order.isRefunded()) {
            send(order.getBuyerId(), "PAYMENT",
                    "Order #" + order.getId() + " is cancelled. Your refund of ₹" + order.getTotalAmount() + " has started.");
        }
    }

    // Cancel in the payment popup, or the payment expired (you)
    public void paymentFailed(List<Order> orders) {
        if (orders.isEmpty()) {
            return;
        }
        Order first = orders.get(0);
        send(first.getBuyerId(), "PAYMENT",
                "Payment for " + first.getCheckoutRef() + " didn't go through, so the order was cancelled. Nothing was charged.");
    }

    private void send(Long userId, String type, String message) {
        try {
            client.create(new NotificationClient.NewNotification(userId, type, message));
        } catch (Exception e) {
            log.warn("Could not notify user {}: {}", userId, e.getMessage());
        }
    }
}