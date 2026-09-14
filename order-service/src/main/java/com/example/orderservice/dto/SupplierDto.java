package com.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Mirrors product-service's SupplierResponse shape (nested address omitted — not needed here). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDto {
    private String name;
    private String contactEmail;
    private String phone;
}
