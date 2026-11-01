package com.revShop.productservice.service;

import com.revShop.productservice.client.NotificationClient;
import com.revShop.productservice.dto.ReservedItem;
import com.revShop.productservice.dto.StockItem;
import com.revShop.productservice.entity.Product;
import com.revShop.productservice.repository.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final ProductRepository productRepository;
    private final NotificationClient notificationClient;

    public StockService(ProductRepository productRepository, NotificationClient notificationClient) {
        this.productRepository = productRepository;
        this.notificationClient = notificationClient;
    }

    // All or nothing: if any item fails, the exception rolls back the ones already taken.
    @Transactional
    public List<ReservedItem> reserve(List<StockItem> items) {
        List<ReservedItem> reserved = new ArrayList<>();
        List<LowStockAlert> lowStock = new ArrayList<>();   // sent once everything is reserved

        for (StockItem item : items) {
            Product product = productRepository.findById(item.productId())
                    .filter(p -> Boolean.TRUE.equals(p.getActive()))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product " + item.productId() + " not found"));

            int updated = productRepository.decrementStock(item.productId(), item.quantity());
            if (updated == 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Not enough stock for " + product.getName());
            }

            reserved.add(new ReservedItem(product.getId(), product.getName(), product.getPrice(),
                    product.getSellerId(), item.quantity()));

            // US-21: tell the seller once, when this sale takes the stock to the threshold or below
            int before = product.getStock();
            int after = before - item.quantity();
            int threshold = product.getLowStockThreshold();
            if (before > threshold && after <= threshold) {
                String message = after == 0
                        ? product.getName() + " is now out of stock. Restock it to keep selling."
                        : "Low stock: only " + after + " of " + product.getName() + " left. Restock it soon.";
                lowStock.add(new LowStockAlert(product.getSellerId(), message));
            }
        }

        for (LowStockAlert alert : lowStock) {
            notifySeller(alert.sellerId(), alert.message());
        }
        return reserved;
    }

    private record LowStockAlert(Long sellerId, String message) {}

    // a failed notification never stops a checkout: it is only logged
    private void notifySeller(Long sellerId, String message) {
        try {
            notificationClient.create(new NotificationClient.NewNotification(sellerId, "LOW_STOCK", message));
        } catch (Exception e) {
            log.warn("Could not send the low-stock notice to seller {}: {}", sellerId, e.getMessage());
        }
    }

    // checkout failed, payment failed, or the buyer cancelled: put it back
    @Transactional
    public void release(List<StockItem> items) {
        for (StockItem item : items) {
            productRepository.incrementStock(item.productId(), item.quantity());
        }
    }
}