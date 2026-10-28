
package com.revShop.productservice.repository;

import com.revShop.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findBySellerId(Long sellerId);

    List<Product> findByActiveTrue();

    boolean existsByCategoryId(Long id);

    Page<Product> findByActiveTrue(Pageable pageable);
}
