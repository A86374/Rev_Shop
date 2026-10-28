
package com.revShop.productservice.specification;

import com.revShop.productservice.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecifications {

    public static Specification<Product> activeProducts() {
        return (root, query, cb) ->
                cb.isTrue(root.get("active"));
    }

    public static Specification<Product> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(root.get("name")),
                    "%" + search.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<Product> filterByCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Product> filterByPrice(
            BigDecimal minPrice, BigDecimal maxPrice) {

        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (minPrice != null) {
                predicate = cb.and(
                        predicate,
                        cb.greaterThanOrEqualTo(
                                root.get("price"), minPrice)
                );
            }

            if (maxPrice != null) {
                predicate = cb.and(
                        predicate,
                        cb.lessThanOrEqualTo(
                                root.get("price"), maxPrice)
                );
            }

            return predicate;
        };
    }
}
