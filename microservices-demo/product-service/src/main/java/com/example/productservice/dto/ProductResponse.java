package com.example.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;

    /** Nested object — may itself contain a nested parentCategory. */
    private CategoryResponse category;

    /** Nested object — contains a nested address. */
    private SupplierResponse supplier;

    /** Nested object. */
    private DimensionsResponse dimensions;

    /** Nested map. */
    private Map<String, String> attributes;

    /** Nested array of scalars. */
    private List<String> tags;

    /** Nested array of objects, each containing a nested reviewer object. */
    private List<ReviewResponse> reviews;

    private Double averageRating;

    private Instant createdAt;
    private Instant updatedAt;
}
