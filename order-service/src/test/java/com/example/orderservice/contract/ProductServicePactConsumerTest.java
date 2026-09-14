package com.example.orderservice.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.example.orderservice.client.ProductClient;
import com.example.orderservice.dto.ProductDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Consumer-driven contract test for order-service's dependency on
 * product-service.
 *
 * Running `mvn test` (or `mvn verify`) in this module executes the two
 * @Test methods below. Each one:
 *   1. Starts an in-process mock HTTP server that behaves exactly as
 *      described in the matching @Pact method.
 *   2. Points the real ProductClient at that mock server.
 *   3. Calls ProductClient exactly like OrderService does in production.
 *   4. Asserts the client parses the response correctly.
 *
 * As a side effect, Pact records every interaction it saw and writes the
 * contract to target/pacts/order-service-product-service.json. That file
 * is the artifact product-service's provider test verifies against — see
 * product-service's ProductServiceProviderPactTest.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "product-service", pactVersion = PactSpecVersion.V3)
class ProductServicePactConsumerTest {

    @Pact(consumer = "order-service")
    RequestResponsePact getProductById(PactDslWithProvider builder) {
        return builder
                .given("product with id 1 exists")
                .uponReceiving("a request for product 1")
                .path("/api/v1/products/1")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(productResponseBody(100))
                .toPact();
    }

    @Pact(consumer = "order-service")
    RequestResponsePact reserveStock(PactDslWithProvider builder) {
        return builder
                .given("product with id 1 exists and has sufficient stock")
                .uponReceiving("a request to reserve 2 units of product 1")
                .path("/api/v1/products/1/reserve-stock")
                .method("POST")
                .headers(Map.of("Content-Type", "application/json"))
                .body("{\"quantity\": 2}")
                .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(productResponseBody(98))
                .toPact();
    }

    /**
     * Only the subset of product-service's (much larger, nested)
     * ProductResponse that order-service's ProductDto actually reads.
     * Pact only asserts on fields declared here — extra fields present
     * in the real response are ignored, so product-service is free to
     * add more nested data without breaking this contract.
     */
    private DslPart productResponseBody(int stockQuantity) {
        return new PactDslJsonBody()
                .numberType("id", 1)
                .stringType("name", "Wireless Mouse")
                .stringType("description", "Ergonomic wireless mouse")
                .decimalType("price", 29.99)
                .numberType("stockQuantity", stockQuantity)
                .object("category")
                    .numberType("id", 2)
                    .stringType("name", "Laptops & Accessories")
                    .stringType("description", "Laptop peripherals")
                    .object("parentCategory")
                        .numberType("id", 1)
                        .stringType("name", "Electronics")
                        .stringType("description", "All electronics")
                        .nullValue("parentCategory")
                    .closeObject()
                .closeObject()
                .object("supplier")
                    .stringType("name", "Acme Peripherals Ltd")
                    .stringType("contactEmail", "sales@acmeperipherals.example")
                    .stringType("phone", "+1-555-0100")
                .closeObject();
    }

    @Test
    @PactTestFor(pactMethod = "getProductById")
    void clientParsesProductResponseCorrectly(MockServer mockServer) {
        ProductClient client = new ProductClient(WebClient.builder().baseUrl(mockServer.getUrl()).build());

        ProductDto product = client.getProduct(1L);

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("Wireless Mouse");
        assertThat(product.getCategory().getName()).isEqualTo("Laptops & Accessories");
        assertThat(product.getCategory().getParentCategory().getName()).isEqualTo("Electronics");
        assertThat(product.getSupplier().getName()).isEqualTo("Acme Peripherals Ltd");
    }

    @Test
    @PactTestFor(pactMethod = "reserveStock")
    void clientReservesStockCorrectly(MockServer mockServer) {
        ProductClient client = new ProductClient(WebClient.builder().baseUrl(mockServer.getUrl()).build());

        ProductDto product = client.reserveStock(1L, 2);

        assertThat(product.getStockQuantity()).isEqualTo(98);
    }
}
