package com.revShop.cartservice.repository;

import com.revShop.cartservice.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUserIdOrderByAddedAtDesc(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);
}