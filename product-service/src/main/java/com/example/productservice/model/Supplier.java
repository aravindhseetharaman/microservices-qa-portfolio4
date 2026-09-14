package com.example.productservice.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Supplier is itself embeddable and contains a nested Address —
 * demonstrates two levels of object nesting inside Product.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Column(name = "supplier_name")
    private String name;

    @Column(name = "supplier_contact_email")
    private String contactEmail;

    @Column(name = "supplier_phone")
    private String phone;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "supplier_street")),
            @AttributeOverride(name = "city", column = @Column(name = "supplier_city")),
            @AttributeOverride(name = "state", column = @Column(name = "supplier_state")),
            @AttributeOverride(name = "zipCode", column = @Column(name = "supplier_zip_code")),
            @AttributeOverride(name = "country", column = @Column(name = "supplier_country"))
    })
    private Address address;
}
