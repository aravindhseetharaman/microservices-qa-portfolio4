package com.example.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DimensionsResponse {
    private Double length;
    private Double width;
    private Double height;
    private Double weight;
    private String unit;
}
