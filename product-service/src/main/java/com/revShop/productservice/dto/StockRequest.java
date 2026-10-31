package com.revShop.productservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// {"items":[{"productId":1,"quantity":2}]} - the same shape order-service sends
public record StockRequest(

        @NotEmpty(message = "At least one item is required")
        List<@Valid StockItem> items

) {}