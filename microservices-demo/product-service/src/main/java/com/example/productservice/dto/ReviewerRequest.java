package com.example.productservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewerRequest {

    @NotBlank(message = "Reviewer name is required")
    private String name;

    @Email(message = "Reviewer email must be valid")
    private String email;
}
