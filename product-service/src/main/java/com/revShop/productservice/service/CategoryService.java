
package com.revShop.productservice.service;

import com.revShop.productservice.dto.CategoryRequest;
import com.revShop.productservice.entity.Category;
import com.revShop.productservice.repository.CategoryRepository;
import com.revShop.productservice.repository.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public Category addCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category already exists"
            );
        }

        Category category = new Category(
                request.name().trim(),
                request.description()
        );

        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found"
                ));
    }

    public Category updateCategory(Long id, CategoryRequest request) {
        Category category = getCategoryById(id);

        boolean nameChanged = !category.getName()
                .equalsIgnoreCase(request.name().trim());

        if (nameChanged &&
                categoryRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category name already exists"
            );
        }

        category.setName(request.name().trim());
        category.setDescription(request.description());

        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);

        if (productRepository.existsByCategoryId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a category that has products"
            );
        }

        categoryRepository.delete(category);
    }
}
