package com.example.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Used for PUT /api/v1/orders/{id}. Updates customer/shipping/billing/payment
 * details on an existing order. Line items are intentionally not editable here
 * since changing them would require re-reserving stock on product-service —
 * cancel and recreate the order for item changes instead.
 */
@Getter
@Setter
public class OrderUpdateRequest {

    @Valid
    @NotNull(message = "Customer details are required")
    private CustomerRequest customer;

    @Valid
    @NotNull(message = "Shipping address is required")
    private AddressRequest shippingAddress;

    @Valid
    @NotNull(message = "Billing address is required")
    private AddressRequest billingAddress;

    @Valid
    private PaymentRequest payment;
}
