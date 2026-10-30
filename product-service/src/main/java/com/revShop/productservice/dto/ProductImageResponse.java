package com.revShop.productservice.dto;

import com.revShop.productservice.entity.ProductImage;

public record ProductImageResponse(Long id, String url) {

    public static ProductImageResponse from(ProductImage image) {
        return new ProductImageResponse(image.getId(), image.getUrl());
    }
}