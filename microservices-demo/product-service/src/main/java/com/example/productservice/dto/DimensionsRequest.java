package com.example.productservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DimensionsRequest {
    private Double length;
    private Double width;
    private Double height;
    private Double weight;
    private String unit;
}
