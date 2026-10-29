package com.revShop.orderservice.repository;

import com.revShop.orderservice.entity.Order;
import com.revShop.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Get a buyer's orders, newest first
    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    // Get all orders of a seller
    List<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    // Get a seller's orders by status
    List<Order> findBySellerIdAndStatusOrderByCreatedAtDesc(
            Long sellerId, OrderStatus status);

    // Get all orders from one checkout
    List<Order> findByCheckoutRef(String checkoutRef);

    // Check if a buyer purchased a product with a given status
    boolean existsByBuyerIdAndStatusAndItems_ProductId(
            Long buyerId, OrderStatus status, Long productId);

    // Get all orders, newest first
    List<Order> findAllByOrderByCreatedAtDesc();

    // Get all orders with a specific status
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
}