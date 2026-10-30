package com.revShop.orderservice.repository;

import com.revShop.orderservice.entity.Order;
import com.revShop.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // US-17 (you): my orders
    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    // US-19 (Hasitha, tomorrow): seller's orders
    List<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    List<Order> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, OrderStatus status);

    // US-15 (you, tomorrow): every order of one checkout, to apply Pay / Cancel
    List<Order> findByCheckoutRef(String checkoutRef);

    // US-20 (you, tomorrow): did this buyer receive this product?
    boolean existsByBuyerIdAndStatusAndItems_ProductId(Long buyerId, OrderStatus status, Long productId);

    // US-25 (Saturday): admin monitoring
    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
}