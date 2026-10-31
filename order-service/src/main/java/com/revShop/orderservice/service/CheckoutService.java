package com.revShop.orderservice.service;

import com.revShop.orderservice.client.CheckoutCartClient;
import com.revShop.orderservice.client.CheckoutCartClient.CartLine;
import com.revShop.orderservice.client.CheckoutPaymentClient;
import com.revShop.orderservice.client.CheckoutStockClient;
import com.revShop.orderservice.client.CheckoutStockClient.ReservedItem;
import com.revShop.orderservice.client.CheckoutStockClient.StockItem;
import com.revShop.orderservice.client.CheckoutStockClient.StockRequest;
import com.revShop.orderservice.dto.CheckoutRequest;
import com.revShop.orderservice.dto.CheckoutResponse;
import com.revShop.orderservice.dto.OrderResponse;
import com.revShop.orderservice.entity.Order;
import com.revShop.orderservice.entity.OrderItem;
import com.revShop.orderservice.entity.OrderStatus;
import com.revShop.orderservice.entity.PaymentMethod;
import com.revShop.orderservice.exception.ApiException;
import com.revShop.orderservice.repository.OrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final OrderRepository orderRepository;
    private final CheckoutCartClient cartClient;
    private final CheckoutStockClient stockClient;
    private final CheckoutPaymentClient paymentClient;

    public CheckoutResponse checkout(Long buyerId, CheckoutRequest request) {
        // 1. what is in the cart
        List<CartLine> cart = loadCart(buyerId);
        if (cart.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Your cart is empty");
        }
        StockRequest wanted = new StockRequest(cart.stream()
                .map(line -> new StockItem(line.productId(), line.quantity()))
                .toList());

        // 2. take the stock: all or nothing, and it gives back the real prices and sellers
        List<ReservedItem> reserved = reserve(wanted);

        String checkoutRef = "CHK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        List<Order> saved = new ArrayList<>();
        try {
            // 3. one order per seller
            OrderStatus start = request.paymentMethod() == PaymentMethod.COD
                    ? OrderStatus.PLACED
                    : OrderStatus.PAYMENT_PENDING;

            Map<Long, List<ReservedItem>> bySeller = reserved.stream()
                    .collect(Collectors.groupingBy(ReservedItem::sellerId, LinkedHashMap::new, Collectors.toList()));

            List<Order> orders = new ArrayList<>();
            for (Map.Entry<Long, List<ReservedItem>> entry : bySeller.entrySet()) {
                Order order = Order.builder()
                        .buyerId(buyerId)
                        .sellerId(entry.getKey())
                        .checkoutRef(checkoutRef)
                        .status(start)
                        .paymentMethod(request.paymentMethod())
                        .shippingAddress(request.shippingAddress().trim())
                        .totalAmount(BigDecimal.ZERO)
                        .build();

                BigDecimal total = BigDecimal.ZERO;
                for (ReservedItem item : entry.getValue()) {
                    order.addItem(OrderItem.builder()
                            .productId(item.productId())
                            .productName(item.name())
                            .price(item.price())
                            .quantity(item.quantity())
                            .build());
                    total = total.add(item.price().multiply(BigDecimal.valueOf(item.quantity())));
                }
                order.setTotalAmount(total);
                orders.add(order);
            }
            saved.addAll(orderRepository.saveAll(orders));

            BigDecimal grandTotal = saved.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 4. one payment for the whole checkout
            CheckoutPaymentClient.PaymentInfo payment = paymentClient.create(new CheckoutPaymentClient.NewPayment(
                    checkoutRef, buyerId, grandTotal, request.paymentMethod().name()));

            // 5. empty the cart; the orders are already safe if this one fails
            try {
                cartClient.clearCart(buyerId);
            } catch (Exception e) {
                log.warn("Could not clear the cart of user {}: {}", buyerId, e.getMessage());
            }

            return new CheckoutResponse(checkoutRef, payment.id(), request.paymentMethod().name(), grandTotal,
                    payment.expiresAt(), saved.stream().map(OrderResponse::from).toList());

        } catch (RuntimeException e) {
            // undo everything, so a half-made checkout never stays behind
            if (!saved.isEmpty()) {
                orderRepository.deleteAll(saved);
            }
            try {
                stockClient.release(wanted);
            } catch (Exception releaseError) {
                log.error("Checkout {} failed and the stock could not be released: {}",
                        checkoutRef, releaseError.getMessage());
            }
            log.error("Checkout {} failed: {}", checkoutRef, e.getMessage());
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Checkout failed and nothing was charged. Please try again.");
        }
    }

    private List<CartLine> loadCart(Long buyerId) {
        try {
            return cartClient.getCart(buyerId);
        } catch (FeignException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Cart Service is not responding, try again");
        }
    }

    private List<ReservedItem> reserve(StockRequest wanted) {
        try {
            return stockClient.reserve(wanted);
        } catch (FeignException e) {
            if (e.status() == 404 || e.status() == 409) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Some items in your cart are out of stock or no longer available");
            }
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Product Service is not responding, try again");
        }
    }
}