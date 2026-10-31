package com.revShop.productservice.controller;

import com.revShop.productservice.dto.ReservedItem;
import com.revShop.productservice.dto.StockRequest;
import com.revShop.productservice.service.StockService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// /internal is not routed by the gateway: only order-service calls these
@RestController
@RequestMapping("/internal/products")
public class InternalStockController {

    private final StockService stockService;

    public InternalStockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/reserve")
    public List<ReservedItem> reserve(@Valid @RequestBody StockRequest request) {
        return stockService.reserve(request.items());
    }

    @PostMapping("/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@Valid @RequestBody StockRequest request) {
        stockService.release(request.items());
    }
}