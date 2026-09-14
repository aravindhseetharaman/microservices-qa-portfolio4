package com.example.orderservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Column(name = "payment_method")
    private String method; // e.g. "CREDIT_CARD", "PAYPAL"

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus status;

    @Column(name = "payment_transaction_id")
    private String transactionId;

    @Column(name = "payment_amount")
    private BigDecimal amount;

    @Column(name = "payment_processed_at")
    private Instant processedAt;
}
