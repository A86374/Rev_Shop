
package com.revShop.cartservice.service;

import com.revShop.cartservice.client.ProductClient;
import com.revShop.cartservice.client.ProductClient.ProductResponse;
import com.revShop.cartservice.entity.CartItem;
import com.revShop.cartservice.repository.CartItemRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;

    public CartService(
            CartItemRepository cartItemRepository,
            ProductClient productClient) {
        this.cartItemRepository = cartItemRepository;
        this.productClient = productClient;
    }

    public CartItem addToCart(Long userId, Long productId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero");
        }

        ProductResponse product = getProduct(productId);

        if (product.active() == null || !product.active()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product is not available");
        }

        CartItem item = cartItemRepository
                .findByUserIdAndProductId(userId, productId)
                .orElseGet(CartItem::new);

        int newQuantity = item.getId() == null
                ? quantity
                : item.getQuantity() + quantity;

        if (newQuantity > product.stock()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requested quantity exceeds available stock");
        }

        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(newQuantity);

        return cartItemRepository.save(item);
    }

    public List<CartItem> getCart(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public CartItem updateQuantity(
            Long userId, Long productId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero");
        }

        ProductResponse product = getProduct(productId);

        if (product.active() == null || !product.active()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product is not available");
        }

        if (quantity > product.stock()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requested quantity exceeds available stock");
        }

        CartItem item = cartItemRepository
                .findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product is not in the cart"));

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    public void removeFromCart(Long userId, Long productId) {

        CartItem item = cartItemRepository
                .findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product is not in the cart"));

        cartItemRepository.delete(item);
    }

    private ProductResponse getProduct(Long productId) {
        try {
            return productClient.getProductById(productId);
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not retrieve product details from Product Service");
        }
    }

    // US-14: checkout empties the cart once the orders exist
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }


}
