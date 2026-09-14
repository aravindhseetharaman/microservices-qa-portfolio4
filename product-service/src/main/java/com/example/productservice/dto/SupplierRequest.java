package com.example.productservice.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRequest {
    private String name;
    private String contactEmail;
    private String phone;

    @Valid
    private AddressRequest address;
}
