
package com.revShop.cartservice.controller;

import com.revShop.cartservice.dto.CartRequest;
import com.revShop.cartservice.entity.CartItem;
import com.revShop.cartservice.service.CartService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/{userId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItem addToCart(
            @PathVariable Long userId,
            @Valid @RequestBody CartRequest request) {

        return cartService.addToCart(
                userId,
                request.productId(),
                request.quantity());
    }

    @GetMapping("/{userId}")
    public List<CartItem> getCart(@PathVariable Long userId) {
        return cartService.getCart(userId);
    }

    @PutMapping("/{userId}/items/{productId}")
    public CartItem updateQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @Valid @RequestBody CartRequest request) {

        if (!productId.equals(request.productId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product ID in URL and request body must match");
        }

        return cartService.updateQuantity(
                userId,
                productId,
                request.quantity());
    }

    @DeleteMapping("/{userId}/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {

        cartService.removeFromCart(userId, productId);
    }
    // US-14: order-service calls this after a successful checkout
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable("userId") Long userId) {
        cartService.clearCart(userId);
    }
}
