package com.example.orderservice.dto;

import com.example.orderservice.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String method;
    private PaymentStatus status;
    private String transactionId;
    private BigDecimal amount;
    private Instant processedAt;
}
