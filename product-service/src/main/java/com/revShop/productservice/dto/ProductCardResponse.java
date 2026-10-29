package com.revShop.productservice.dto;

import com.revShop.productservice.entity.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ProductCardResponse(
        Long id,
        String name,
        BigDecimal price,
        BigDecimal mrp,
        int discountPercent,
        String categoryName,
        String imageUrl,          // first photo, or null
        StockStatus stockStatus,
        String stockLabel
) {
    public static ProductCardResponse from(Product p, String imageUrl) {
        return new ProductCardResponse(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getMrp(),
                discountPercent(p.getMrp(), p.getPrice()),
                p.getCategory().getName(),
                imageUrl,
                StockStatus.of(p.getStock(), p.getLowStockThreshold()),
                StockStatus.label(p.getStock(), p.getLowStockThreshold())
        );
    }

    // MRP 999, price 799 -> 20 (% off, rounded down)
    public static int discountPercent(BigDecimal mrp, BigDecimal price) {
        if (mrp == null || price == null || mrp.signum() <= 0 || price.compareTo(mrp) >= 0) {
            return 0;
        }
        return mrp.subtract(price)
                .multiply(BigDecimal.valueOf(100))
                .divide(mrp, 0, RoundingMode.DOWN)
                .intValue();
    }
}