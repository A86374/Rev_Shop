
package com.revShop.productservice.service;

import com.revShop.productservice.dto.ProductRequest;
import com.revShop.productservice.entity.Category;
import com.revShop.productservice.entity.Product;
import com.revShop.productservice.repository.CategoryRepository;
import com.revShop.productservice.repository.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Product addProduct(ProductRequest request, Long sellerId) {

        if (request.price().compareTo(request.mrp()) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selling price cannot be greater than MRP"
            );
        }

        if (request.stock() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Stock cannot be negative"
            );
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found"
                ));

        Product product = new Product();
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setCategory(category);
        product.setMrp(request.mrp());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setLowStockThreshold(request.lowStockThreshold());
        product.setSellerId(sellerId);
        product.setActive(true);

        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }

    public Product getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found"
            );
        }

        return product;
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .filter(product -> Boolean.TRUE.equals(product.getActive()))
                .toList();
    }

    public Page<Product> getProductsPage(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }
}
