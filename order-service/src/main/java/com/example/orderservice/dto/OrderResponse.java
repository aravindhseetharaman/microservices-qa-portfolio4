package com.example.orderservice.dto;

import com.example.orderservice.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;

    /** Nested object. */
    private CustomerResponse customer;

    /** Nested object. */
    private AddressResponse shippingAddress;

    /** Nested object. */
    private AddressResponse billingAddress;

    /** Nested object. */
    private PaymentResponse payment;

    private OrderStatus status;
    private BigDecimal totalAmount;

    /** Nested array of objects, each with its own nested productSnapshot object. */
    private List<OrderItemResponse> items;

    private Instant createdAt;
    private Instant updatedAt;
}
