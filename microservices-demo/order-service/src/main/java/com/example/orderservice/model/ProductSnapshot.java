package com.example.orderservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * A nested snapshot of product details as they were at the time of purchase,
 * captured from product-service's (itself nested) response.
 */
@jakarta.persistence.Embeddable
@lombok.Getter
@lombok.Setter
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class ProductSnapshot {

    @Column(name = "snap_category_name")
    private String categoryName;

    @Column(name = "snap_supplier_name")
    private String supplierName;
}
