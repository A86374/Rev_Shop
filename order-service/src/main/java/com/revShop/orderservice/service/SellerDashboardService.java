package com.revShop.orderservice.service;

import com.revShop.orderservice.dto.OrderResponse;
import com.revShop.orderservice.dto.SellerDashboardResponse;
import com.revShop.orderservice.dto.SellerDashboardResponse.TopProduct;
import com.revShop.orderservice.entity.Order;
import com.revShop.orderservice.entity.OrderItem;
import com.revShop.orderservice.entity.OrderStatus;
import com.revShop.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerDashboardService {

    // orders that are real sales (paid or cash on delivery, and not cancelled)
    private static final Set<OrderStatus> SOLD = Set.of(OrderStatus.PLACED, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final OrderRepository orderRepository;

    // US-22: everything the dashboard shows, worked out from the seller's orders
    @Transactional(readOnly = true)
    public SellerDashboardResponse dashboard(Long sellerId) {
        List<Order> orders = orderRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);

        Map<OrderStatus, Long> counts = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus,
                        () -> new EnumMap<>(OrderStatus.class), Collectors.counting()));

        BigDecimal revenue = total(orders, Set.of(OrderStatus.DELIVERED));
        BigDecimal pendingRevenue = total(orders, Set.of(OrderStatus.PLACED, OrderStatus.SHIPPED));

        long itemsSold = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .flatMap(o -> o.getItems().stream())
                .mapToLong(OrderItem::getQuantity)
                .sum();

        // best sellers: units per product across every real sale
        Map<Long, TopProduct> byProduct = new LinkedHashMap<>();
        for (Order order : orders) {
            if (!SOLD.contains(order.getStatus())) {
                continue;
            }
            for (OrderItem item : order.getItems()) {
                BigDecimal line = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                byProduct.merge(item.getProductId(),
                        new TopProduct(item.getProductId(), item.getProductName(), item.getQuantity(), line),
                        (a, b) -> new TopProduct(a.productId(), a.productName(),
                                a.unitsSold() + b.unitsSold(), a.revenue().add(b.revenue())));
            }
        }
        List<TopProduct> topProducts = byProduct.values().stream()
                .sorted(Comparator.comparingLong(TopProduct::unitsSold).reversed())
                .limit(5)
                .toList();

        List<OrderResponse> recentOrders = orders.stream()
                .limit(5)
                .map(OrderResponse::from)
                .toList();

        return new SellerDashboardResponse(
                counts.getOrDefault(OrderStatus.PLACED, 0L),
                counts.getOrDefault(OrderStatus.SHIPPED, 0L),
                counts.getOrDefault(OrderStatus.DELIVERED, 0L),
                counts.getOrDefault(OrderStatus.CANCELLED, 0L),
                counts.getOrDefault(OrderStatus.PAYMENT_PENDING, 0L),
                revenue,
                pendingRevenue,
                itemsSold,
                topProducts,
                recentOrders);
    }

    private BigDecimal total(List<Order> orders, Set<OrderStatus> statuses) {
        return orders.stream()
                .filter(o -> statuses.contains(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}