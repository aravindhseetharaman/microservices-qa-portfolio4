package com.example.productservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
