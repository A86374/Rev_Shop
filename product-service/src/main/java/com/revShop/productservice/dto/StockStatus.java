package com.revShop.productservice.dto;

public enum StockStatus {
    IN_STOCK, LOW_STOCK, OUT_OF_STOCK;

    public static StockStatus of(int stock, int lowStockThreshold) {
        if (stock <= 0) {
            return OUT_OF_STOCK;
        }
        if (stock <= lowStockThreshold) {
            return LOW_STOCK;
        }
        return IN_STOCK;
    }

    // the text shown on the product card and details page
    public static String label(int stock, int lowStockThreshold) {
        return switch (of(stock, lowStockThreshold)) {
            case OUT_OF_STOCK -> "Out of stock";
            case LOW_STOCK -> "Only " + stock + " left";
            case IN_STOCK -> "In stock";
        };
    }
}