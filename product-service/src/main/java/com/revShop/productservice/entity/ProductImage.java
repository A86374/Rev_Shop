package com.revShop.productservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    // path of the file inside the bucket, needed to delete it later
    @Column(nullable = false, length = 300)
    private String s3Key;

    @Column(nullable = false, length = 500)
    private String url;

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}