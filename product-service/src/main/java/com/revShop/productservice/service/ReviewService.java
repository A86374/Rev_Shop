package com.revShop.productservice.service;

import com.revShop.productservice.client.OrderCheckClient;
import com.revShop.productservice.dto.ReviewRequest;
import com.revShop.productservice.dto.ReviewResponse;
import com.revShop.productservice.entity.Review;
import com.revShop.productservice.repository.ProductRepository;
import com.revShop.productservice.repository.ReviewRepository;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderCheckClient orderCheckClient;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         OrderCheckClient orderCheckClient) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.orderCheckClient = orderCheckClient;
    }

    public ReviewResponse add(Long buyerId, ReviewRequest request) {
        productRepository.findById(request.productId())
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (reviewRepository.existsByProductIdAndBuyerId(request.productId(), buyerId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already reviewed this product");
        }

        boolean delivered;
        try {
            delivered = orderCheckClient.deliveredCheck(buyerId, request.productId()).delivered();
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not check your orders, try again");
        }
        if (!delivered) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only review products from your delivered orders");
        }

        Review review = new Review();
        review.setProductId(request.productId());
        review.setBuyerId(buyerId);
        review.setRating(request.rating());
        review.setComment(request.comment() == null || request.comment().isBlank() ? null : request.comment().trim());

        return ReviewResponse.from(reviewRepository.save(review));
    }

    public ReviewResponse.ProductReviews forProduct(Long productId) {
        List<ReviewResponse> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(ReviewResponse::from)
                .toList();
        double average = reviews.stream().mapToInt(ReviewResponse::rating).average().orElse(0);
        return new ReviewResponse.ProductReviews(productId, Math.round(average * 10) / 10.0, reviews.size(), reviews);
    }
}