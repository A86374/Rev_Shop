package com.revShop.productservice.dto;

import com.revShop.productservice.entity.Review;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(Long id, Long productId, Long buyerId, Integer rating,
                             String comment, LocalDateTime createdAt) {

    public static ReviewResponse from(Review r) {
        return new ReviewResponse(r.getId(), r.getProductId(), r.getBuyerId(), r.getRating(),
                r.getComment(), r.getCreatedAt());
    }

    // what the product page shows: average, count and the list
    public record ProductReviews(Long productId, double averageRating, int count, List<ReviewResponse> reviews) {
    }
}