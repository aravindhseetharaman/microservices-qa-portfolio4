package com.example.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    @NotBlank(message = "Payment method is required")
    private String method; // e.g. "CREDIT_CARD", "PAYPAL"
}
