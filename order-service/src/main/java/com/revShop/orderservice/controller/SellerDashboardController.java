package com.revShop.orderservice.controller;

import com.revShop.orderservice.dto.SellerDashboardResponse;
import com.revShop.orderservice.service.SellerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/dashboard")
@RequiredArgsConstructor
public class SellerDashboardController {

    private final SellerDashboardService dashboardService;

    @GetMapping
    public SellerDashboardResponse dashboard(@RequestHeader("X-User-Id") Long sellerId) {
        return dashboardService.dashboard(sellerId);
    }
}