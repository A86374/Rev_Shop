package com.revShop.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record SellerDashboardResponse(
        long toShip,                // PLACED: waiting for the seller
        long shipped,
        long delivered,
        long cancelled,
        long awaitingPayment,       // online orders the buyer hasn't paid yet
        BigDecimal revenue,         // delivered orders
        BigDecimal pendingRevenue,  // placed or shipped, not delivered yet
        long itemsSold,             // units in delivered orders
        List<TopProduct> topProducts,
        List<OrderResponse> recentOrders
) {
    public record TopProduct(Long productId, String productName, long unitsSold, BigDecimal revenue) {}
}