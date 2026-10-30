package com.revShop.cartservice.controller;

import com.revShop.cartservice.dto.WishlistItemResponse;
import com.revShop.cartservice.dto.WishlistRequest;
import com.revShop.cartservice.service.WishlistService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistItemResponse add(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody WishlistRequest request) {

        return wishlistService.add(userId, request.productId());
    }

    @GetMapping
    public List<WishlistItemResponse> getWishlist(@RequestHeader("X-User-Id") Long userId) {
        return wishlistService.getWishlist(userId);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("productId") Long productId) {

        wishlistService.remove(userId, productId);
    }
}