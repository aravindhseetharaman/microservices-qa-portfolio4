package com.example.orderservice.service;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.dto.*;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.model.*;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        return toResponse(findOrderOrThrow(id));
    }

    /**
     * Creates an order by:
     *  1. Looking up each product's current (nested) details from product-service
     *  2. Reserving stock for each item on product-service
     *  3. Persisting the order with nested customer/address/payment objects and
     *     a nested product snapshot per line item
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Order order = new Order();
        order.setCustomer(new Customer(
                request.getCustomer().getName(),
                request.getCustomer().getEmail(),
                request.getCustomer().getPhone()));
        order.setShippingAddress(toAddress(request.getShippingAddress()));
        order.setBillingAddress(toAddress(request.getBillingAddress()));
        order.setStatus(OrderStatus.CREATED);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            // Look up nested product details from product-service
            ProductDto product = productClient.getProduct(itemRequest.getProductId());

            // Reserve stock on product-service (decrements inventory there)
            productClient.reserveStock(itemRequest.getProductId(), itemRequest.getQuantity());

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setProductSnapshot(new ProductSnapshot(
                    product.getCategory() != null ? product.getCategory().getName() : null,
                    product.getSupplier() != null ? product.getSupplier().getName() : null));
            order.addItem(item);

            total = total.add(item.getSubtotal());
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.CONFIRMED);

        order.setPayment(new Payment(
                request.getPayment().getMethod(),
                PaymentStatus.PAID,
                "TXN-" + System.currentTimeMillis(),
                total,
                Instant.now()));

        return toResponse(orderRepository.save(order));
    }

    /** Updates customer/shipping/billing/payment details on an existing order. */
    @Transactional
    public OrderResponse updateOrder(Long id, OrderUpdateRequest request) {
        Order order = findOrderOrThrow(id);

        order.setCustomer(new Customer(
                request.getCustomer().getName(),
                request.getCustomer().getEmail(),
                request.getCustomer().getPhone()));
        order.setShippingAddress(toAddress(request.getShippingAddress()));
        order.setBillingAddress(toAddress(request.getBillingAddress()));

        if (request.getPayment() != null && order.getPayment() != null) {
            order.getPayment().setMethod(request.getPayment().getMethod());
        }

        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = findOrderOrThrow(id);
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = findOrderOrThrow(id);
        orderRepository.delete(order);
    }

    private Address toAddress(AddressRequest a) {
        return new Address(a.getStreet(), a.getCity(), a.getState(), a.getZipCode(), a.getCountry());
    }

    private Order findOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getProductId(), i.getProductName(), i.getQuantity(), i.getUnitPrice(),
                        i.getSubtotal(),
                        i.getProductSnapshot() != null
                                ? new ProductSnapshotResponse(
                                        i.getProductSnapshot().getCategoryName(),
                                        i.getProductSnapshot().getSupplierName())
                                : null))
                .collect(Collectors.toList());

        CustomerResponse customerResponse = order.getCustomer() != null
                ? new CustomerResponse(order.getCustomer().getName(), order.getCustomer().getEmail(), order.getCustomer().getPhone())
                : null;

        AddressResponse shippingResponse = toAddressResponse(order.getShippingAddress());
        AddressResponse billingResponse = toAddressResponse(order.getBillingAddress());

        PaymentResponse paymentResponse = order.getPayment() != null
                ? new PaymentResponse(
                        order.getPayment().getMethod(), order.getPayment().getStatus(),
                        order.getPayment().getTransactionId(), order.getPayment().getAmount(),
                        order.getPayment().getProcessedAt())
                : null;

        return new OrderResponse(
                order.getId(), customerResponse, shippingResponse, billingResponse, paymentResponse,
                order.getStatus(), order.getTotalAmount(), items,
                order.getCreatedAt(), order.getUpdatedAt());
    }

    private AddressResponse toAddressResponse(Address a) {
        if (a == null) {
            return null;
        }
        return new AddressResponse(a.getStreet(), a.getCity(), a.getState(), a.getZipCode(), a.getCountry());
    }
}
