package com.revShop.cartservice.service;

import com.revShop.cartservice.client.ProductClient;
import com.revShop.cartservice.client.ProductClient.ProductResponse;
import com.revShop.cartservice.dto.WishlistItemResponse;
import com.revShop.cartservice.entity.WishlistItem;
import com.revShop.cartservice.repository.WishlistItemRepository;
import feign.FeignException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistRepository;
    private final ProductClient productClient;

    public WishlistService(
            WishlistItemRepository wishlistRepository,
            ProductClient productClient) {
        this.wishlistRepository = wishlistRepository;
        this.productClient = productClient;
    }

    public WishlistItemResponse add(Long userId, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product is already in your wishlist");
        }

        ProductResponse product = getProduct(productId);

        WishlistItem item = new WishlistItem();
        item.setUserId(userId);
        item.setProductId(productId);
        WishlistItem saved = wishlistRepository.save(item);

        return toResponse(saved, product);
    }

    public List<WishlistItemResponse> getWishlist(Long userId) {
        return wishlistRepository.findByUserIdOrderByAddedAtDesc(userId)
                .stream()
                .map(item -> {
                    try {
                        return toResponse(item, productClient.getProductById(item.getProductId()));
                    } catch (FeignException ex) {
                        // removed or deactivated: keep it in the list, marked unavailable
                        return new WishlistItemResponse(item.getProductId(), null, null, false, item.getAddedAt());
                    }
                })
                .toList();
    }

    public void remove(Long userId, Long productId) {
        WishlistItem item = wishlistRepository
                .findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product is not in your wishlist"));

        wishlistRepository.delete(item);
    }

    private WishlistItemResponse toResponse(WishlistItem item, ProductResponse product) {
        boolean available = Boolean.TRUE.equals(product.active())
                && product.stock() != null && product.stock() > 0;
        return new WishlistItemResponse(item.getProductId(), product.name(), product.price(),
                available, item.getAddedAt());
    }

    // 404 from product-service = unknown or inactive product; anything else = service down
    private ProductResponse getProduct(Long productId) {
        try {
            return productClient.getProductById(productId);
        } catch (FeignException.NotFound ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found");
        } catch (FeignException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not retrieve product details from Product Service");
        }
    }
}