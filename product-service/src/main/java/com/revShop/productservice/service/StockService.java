package com.revShop.productservice.service;

import com.revShop.productservice.dto.ReservedItem;
import com.revShop.productservice.dto.StockItem;
import com.revShop.productservice.entity.Product;
import com.revShop.productservice.repository.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockService {

    private final ProductRepository productRepository;

    public StockService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // All or nothing: if any item fails, the exception rolls back the ones already taken.
    @Transactional
    public List<ReservedItem> reserve(List<StockItem> items) {
        List<ReservedItem> reserved = new ArrayList<>();

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
        }
        return reserved;
    }

    // checkout failed, payment failed, or the buyer cancelled: put it back
    @Transactional
    public void release(List<StockItem> items) {
        for (StockItem item : items) {
            productRepository.incrementStock(item.productId(), item.quantity());
        }
    }
}