package com.example.productservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dimensions {

    @Column(name = "dim_length")
    private Double length;

    @Column(name = "dim_width")
    private Double width;

    @Column(name = "dim_height")
    private Double height;

    @Column(name = "dim_weight")
    private Double weight;

    @Column(name = "dim_unit")
    private String unit; // e.g. "cm/kg" or "in/lb"
}
