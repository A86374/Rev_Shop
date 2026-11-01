package com.revShop.productservice.service;

import com.revShop.productservice.dto.ProductImageResponse;
import com.revShop.productservice.entity.ProductImage;
import com.revShop.productservice.repository.ProductImageRepository;
import com.revShop.productservice.repository.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductImageService {

    private static final int MAX_PHOTOS = 5;
    private static final long MAX_BYTES = 2L * 1024 * 1024;   // US-06: 2 MB per photo

    // allowed photo types and the file extension used in S3
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final S3StorageService storage;

    public ProductImageService(
            ProductImageRepository imageRepository,
            ProductRepository productRepository,
            ProductService productService,
            S3StorageService storage) {
        this.imageRepository = imageRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.storage = storage;
    }

    public ProductImageResponse upload(Long productId, Long sellerId, MultipartFile file) {
        productService.getOwnedProduct(productId, sellerId);   // 404 or 403

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Choose a photo to upload"
            );
        }

        String type = file.getContentType();
        if (type == null || !ALLOWED_TYPES.containsKey(type)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only JPG, PNG or WEBP photos are allowed"
            );
        }

        if (file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Each photo must be 2 MB or smaller"
            );
        }

        if (imageRepository.countByProductId(productId) >= MAX_PHOTOS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A product can have at most " + MAX_PHOTOS + " photos"
            );
        }

        String key = "products/" + productId + "/" + UUID.randomUUID() + ALLOWED_TYPES.get(type);
        String url = storage.upload(key, file);

        ProductImage image = new ProductImage();
        image.setProductId(productId);
        image.setS3Key(key);
        image.setUrl(url);

        return ProductImageResponse.from(imageRepository.save(image));
    }

    public List<ProductImageResponse> getImages(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found"
            );
        }

        return imageRepository.findByProductIdOrderByIdAsc(productId)
                .stream()
                .map(ProductImageResponse::from)
                .toList();
    }

    public void delete(Long productId, Long imageId, Long sellerId) {
        productService.getOwnedProduct(productId, sellerId);   // 404 or 403

        ProductImage image = imageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Photo not found"
                ));

        storage.delete(image.getS3Key());
        imageRepository.delete(image);
    }
}