package com.example.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    /** Optional — set to nest this category under a parent. */
    private Long parentCategoryId;
}
