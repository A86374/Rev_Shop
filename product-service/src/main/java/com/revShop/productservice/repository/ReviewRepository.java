package com.revShop.productservice.repository;

import com.revShop.productservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    boolean existsByProductIdAndBuyerId(Long productId, Long buyerId);
}