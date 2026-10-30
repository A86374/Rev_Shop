package com.revShop.productservice.controller;

import com.revShop.productservice.dto.ProductImageResponse;
import com.revShop.productservice.service.ProductImageService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductImageController {

    private final ProductImageService imageService;

    public ProductImageController(ProductImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(value = "/seller/products/{productId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProductImageResponse upload(
            @PathVariable("productId") Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long sellerId) {

        return imageService.upload(productId, sellerId, file);
    }

    @GetMapping("/products/{productId}/images")
    public List<ProductImageResponse> getImages(
            @PathVariable("productId") Long productId) {

        return imageService.getImages(productId);
    }

    @DeleteMapping("/seller/products/{productId}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("productId") Long productId,
            @PathVariable("imageId") Long imageId,
            @RequestHeader("X-User-Id") Long sellerId) {

        imageService.delete(productId, imageId, sellerId);
    }
}