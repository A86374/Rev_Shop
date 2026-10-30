package com.revShop.productservice.dto;

import java.util.List;

// a small, stable page shape for Angular (instead of returning Spring's Page)
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}