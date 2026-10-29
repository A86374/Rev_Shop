
package com.revShop.productservice.repository;

import com.revShop.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findBySellerId(Long sellerId);

    List<Product> findByActiveTrue();

    boolean existsByCategoryId(Long id);

    Page<Product> findByActiveTrue(Pageable pageable);


    // US-09: every filter is optional. A null parameter switches that filter off.
    // Sorting and paging come from the Pageable.
    @Query("""
            select p from Product p
            where p.active = true
              and (:keyword is null
                   or lower(p.name) like lower(concat('%', :keyword, '%'))
                   or lower(p.description) like lower(concat('%', :keyword, '%')))
              and (:categoryId is null or p.category.id = :categoryId)
              and (:minPrice is null or p.price >= :minPrice)
              and (:maxPrice is null or p.price <= :maxPrice)
            """)
    Page<Product> search(@Param("keyword") String keyword,
                         @Param("categoryId") Long categoryId,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         Pageable pageable);
}
