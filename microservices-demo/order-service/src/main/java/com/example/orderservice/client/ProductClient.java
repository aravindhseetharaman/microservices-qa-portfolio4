package com.example.orderservice.client;

import com.example.orderservice.dto.ProductDto;
import com.example.orderservice.dto.StockReserveRequest;
import com.example.orderservice.exception.ProductNotFoundException;
import com.example.orderservice.exception.ProductServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * HTTP client for calling product-service. This is the piece that turns
 * order-service and product-service into two cooperating microservices
 * rather than two standalone apps: creating an order calls out over HTTP
 * to look up product details and reserve stock.
 */
@Component
@RequiredArgsConstructor
public class ProductClient {

    private final WebClient productServiceWebClient;

    public ProductDto getProduct(Long productId) {
        try {
            return productServiceWebClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new ProductNotFoundException(productId);
        } catch (Exception ex) {
            throw new ProductServiceException(
                    "Failed to reach product-service for product " + productId, ex);
        }
    }

    public ProductDto reserveStock(Long productId, int quantity) {
        try {
            return productServiceWebClient.post()
                    .uri("/api/v1/products/{id}/reserve-stock", productId)
                    .bodyValue(new StockReserveRequest(quantity))
                    .retrieve()
                    .bodyToMono(ProductDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new ProductNotFoundException(productId);
        } catch (WebClientResponseException.Conflict ex) {
            throw new ProductServiceException(
                    "Insufficient stock for product " + productId, ex);
        } catch (Exception ex) {
            throw new ProductServiceException(
                    "Failed to reserve stock for product " + productId, ex);
        }
    }
}
