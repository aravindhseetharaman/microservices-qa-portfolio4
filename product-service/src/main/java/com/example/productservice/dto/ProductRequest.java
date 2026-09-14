package com.example.productservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    /** Optional — references an existing category by id. */
    private Long categoryId;

    /** Nested object. */
    @Valid
    private SupplierRequest supplier;

    /** Nested object. */
    @Valid
    private DimensionsRequest dimensions;

    /** Nested map of free-form attributes, e.g. {"color": "black"}. */
    private Map<String, String> attributes;

    /** Nested array of tags. */
    private List<String> tags;
}
