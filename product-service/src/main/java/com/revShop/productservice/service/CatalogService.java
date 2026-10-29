package com.revShop.productservice.service;

import com.revShop.productservice.dto.PagedResponse;
import com.revShop.productservice.dto.ProductCardResponse;
import com.revShop.productservice.dto.ProductDetailsResponse;
import com.revShop.productservice.entity.Product;
import com.revShop.productservice.entity.ProductImage;
import com.revShop.productservice.repository.ProductImageRepository;
import com.revShop.productservice.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CatalogService {

    // allowed values of ?sort=
    private static final Map<String, Sort> SORTS = Map.of(
            "newest", Sort.by("id").descending(),
            "price_asc", Sort.by("price").ascending(),
            "price_desc", Sort.by("price").descending(),
            "name", Sort.by("name").ascending()
    );

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public CatalogService(
            ProductRepository productRepository,
            ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    // US-08, US-09: one page of product cards with optional search, filters and sort
    public PagedResponse<ProductCardResponse> search(String keyword, Long categoryId,
                                                     BigDecimal minPrice, BigDecimal maxPrice,
                                                     String sort, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be non-negative and size must be between 1 and 100"
            );
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Min price cannot be greater than max price"
            );
        }
        Sort order = SORTS.get(sort);
        if (order == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sort must be one of: newest, price_asc, price_desc, name"
            );
        }

        String term = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Product> result = productRepository.search(
                term, categoryId, minPrice, maxPrice, PageRequest.of(page, size, order));

        // first photo of each product on this page, in one query
        List<Long> ids = result.getContent().stream().map(Product::getId).toList();
        Map<Long, String> firstPhoto = new HashMap<>();
        if (!ids.isEmpty()) {
            for (ProductImage image : productImageRepository.findByProductIdInOrderByIdAsc(ids)) {
                firstPhoto.putIfAbsent(image.getProductId(), image.getUrl());
            }
        }

        List<ProductCardResponse> cards = result.getContent().stream()
                .map(p -> ProductCardResponse.from(p, firstPhoto.get(p.getId())))
                .toList();

        return new PagedResponse<>(cards, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    // US-10: everything the product page needs, including all photos
    public ProductDetailsResponse getDetails(Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        List<String> images = productImageRepository.findByProductIdOrderByIdAsc(id)
                .stream()
                .map(ProductImage::getUrl)
                .toList();

        return ProductDetailsResponse.from(product, images);
    }
}