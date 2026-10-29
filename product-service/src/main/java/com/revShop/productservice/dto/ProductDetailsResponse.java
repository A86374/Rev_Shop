package com.revShop.productservice.dto;

import com.revShop.productservice.entity.Product;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailsResponse(
        Long id,
        String name,
        String description,
        Long categoryId,
        String categoryName,
        BigDecimal mrp,
        BigDecimal price,
        int discountPercent,
        Integer stock,
        StockStatus stockStatus,
        String stockLabel,
        Long sellerId,
        List<String> images       // photo URLs, first one is the main photo
) {
    public static ProductDetailsResponse from(Product p, List<String> images) {
        return new ProductDetailsResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getCategory().getId(),
                p.getCategory().getName(),
                p.getMrp(),
                p.getPrice(),
                ProductCardResponse.discountPercent(p.getMrp(), p.getPrice()),
                p.getStock(),
                StockStatus.of(p.getStock(), p.getLowStockThreshold()),
                StockStatus.label(p.getStock(), p.getLowStockThreshold()),
                p.getSellerId(),
                images
        );
    }
}