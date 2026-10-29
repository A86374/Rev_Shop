package com.revShop.productservice.repository;

import com.revShop.productservice.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByIdAsc(Long productId);

    // first photo of each product for the catalogue cards (Nikitha)
    List<ProductImage> findByProductIdInOrderByIdAsc(Collection<Long> productIds);

    Optional<ProductImage> findByIdAndProductId(Long id, Long productId);

    long countByProductId(Long productId);
}