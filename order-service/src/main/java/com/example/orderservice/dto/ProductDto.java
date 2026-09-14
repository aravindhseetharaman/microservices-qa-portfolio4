package com.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Mirrors the nested ProductResponse shape returned by product-service.
 * Only the fields order-service actually needs are declared here — Jackson
 * will ignore any extra nested fields (reviews, attributes, tags, etc.)
 * present in the upstream response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private CategoryDto category;
    private SupplierDto supplier;
}
