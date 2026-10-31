package com.revShop.productservice.controller;

import com.revShop.productservice.dto.ReviewRequest;
import com.revShop.productservice.dto.ReviewResponse;
import com.revShop.productservice.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse add(@RequestHeader("X-User-Id") Long buyerId,
                              @Valid @RequestBody ReviewRequest request) {
        return reviewService.add(buyerId, request);
    }

    @GetMapping("/product/{productId}")
    public ReviewResponse.ProductReviews forProduct(@PathVariable("productId") Long productId) {
        return reviewService.forProduct(productId);
    }
}