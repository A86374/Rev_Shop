package com.revShop.productservice.controller;

import com.revShop.productservice.dto.PagedResponse;
import com.revShop.productservice.dto.ProductCardResponse;
import com.revShop.productservice.dto.ProductDetailsResponse;
import com.revShop.productservice.service.CatalogService;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // /api/products/search?keyword=mouse&categoryId=1&minPrice=100&maxPrice=1000&sort=price_asc&page=0&size=12
    @GetMapping("/search")
    public PagedResponse<ProductCardResponse> search(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "sort", defaultValue = "newest") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size) {

        return catalogService.search(keyword, categoryId, minPrice, maxPrice, sort, page, size);
    }

    @GetMapping("/{id}/details")
    public ProductDetailsResponse getDetails(@PathVariable("id") Long id) {
        return catalogService.getDetails(id);
    }
}