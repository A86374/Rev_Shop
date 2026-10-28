
package com.revShop.productservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 150, message = "Product name cannot exceed 150 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "Category ID is required")
        @PositiveOrZero(message = "Category ID cannot be negative")
        Long categoryId,

        @NotNull(message = "MRP is required")
        @DecimalMin(value = "0.01", message = "MRP must be greater than zero")
        BigDecimal mrp,

        @NotNull(message = "Selling price is required")
        @DecimalMin(value = "0.01", message = "Selling price must be greater than zero")
        BigDecimal price,

        @NotNull(message = "Stock is required")
        @PositiveOrZero(message = "Stock cannot be negative")
        Integer stock,

        @NotNull(message = "Low stock threshold is required")
        @PositiveOrZero(message = "Low stock threshold cannot be negative")
        Integer lowStockThreshold

) {}
