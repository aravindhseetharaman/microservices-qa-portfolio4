package com.example.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {

    /** Nested object. */
    @Valid
    @NotNull(message = "Customer details are required")
    private CustomerRequest customer;

    /** Nested object. */
    @Valid
    @NotNull(message = "Shipping address is required")
    private AddressRequest shippingAddress;

    /** Nested object. */
    @Valid
    @NotNull(message = "Billing address is required")
    private AddressRequest billingAddress;

    /** Nested object. */
    @Valid
    @NotNull(message = "Payment details are required")
    private PaymentRequest payment;

    /** Nested array of objects. */
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;
}
