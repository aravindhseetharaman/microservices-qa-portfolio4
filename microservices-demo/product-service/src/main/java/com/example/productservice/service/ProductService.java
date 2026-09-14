package com.example.productservice.service;

import com.example.productservice.dto.*;
import com.example.productservice.exception.CategoryNotFoundException;
import com.example.productservice.exception.InsufficientStockException;
import com.example.productservice.exception.ProductNotFoundException;
import com.example.productservice.model.*;
import com.example.productservice.repository.CategoryRepository;
import com.example.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        return toResponse(findProductOrThrow(id));
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductOrThrow(id);
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.delete(findProductOrThrow(id));
    }

    /** Called by order-service when an order reserves stock. */
    @Transactional
    public ProductResponse reserveStock(Long id, int quantity) {
        Product product = findProductOrThrow(id);
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(id, product.getStockQuantity(), quantity);
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        return toResponse(productRepository.save(product));
    }

    /** Adds a nested review (with its own nested reviewer object) to a product. */
    @Transactional
    public ProductResponse addReview(Long productId, ReviewRequest request) {
        Product product = findProductOrThrow(productId);

        Review review = new Review();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setReviewer(new Reviewer(request.getReviewer().getName(), request.getReviewer().getEmail()));
        product.addReview(review);

        return toResponse(productRepository.save(product));
    }

    public List<ReviewResponse> getReviews(Long productId) {
        Product product = findProductOrThrow(productId);
        return product.getReviews().stream().map(this::toReviewResponse).collect(Collectors.toList());
    }

    // ---------- mapping helpers ----------

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        if (request.getSupplier() != null) {
            SupplierRequest s = request.getSupplier();
            Address address = null;
            if (s.getAddress() != null) {
                AddressRequest a = s.getAddress();
                address = new Address(a.getStreet(), a.getCity(), a.getState(), a.getZipCode(), a.getCountry());
            }
            product.setSupplier(new Supplier(s.getName(), s.getContactEmail(), s.getPhone(), address));
        } else {
            product.setSupplier(null);
        }

        if (request.getDimensions() != null) {
            DimensionsRequest d = request.getDimensions();
            product.setDimensions(new Dimensions(d.getLength(), d.getWidth(), d.getHeight(), d.getWeight(), d.getUnit()));
        } else {
            product.setDimensions(null);
        }

        Map<String, String> attributes = request.getAttributes() != null
                ? new HashMap<>(request.getAttributes())
                : new HashMap<>();
        product.setAttributes(attributes);

        product.setTags(request.getTags() != null ? request.getTags() : List.of());
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private ProductResponse toResponse(Product p) {
        CategoryResponse categoryResponse = toCategoryResponse(p.getCategory());
        SupplierResponse supplierResponse = toSupplierResponse(p.getSupplier());
        DimensionsResponse dimensionsResponse = toDimensionsResponse(p.getDimensions());
        List<ReviewResponse> reviewResponses = p.getReviews().stream()
                .map(this::toReviewResponse)
                .collect(Collectors.toList());
        Double averageRating = reviewResponses.isEmpty() ? null
                : reviewResponses.stream().mapToInt(ReviewResponse::getRating).average().orElse(0);

        return new ProductResponse(
                p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStockQuantity(),
                categoryResponse, supplierResponse, dimensionsResponse,
                p.getAttributes(), p.getTags(), reviewResponses, averageRating,
                p.getCreatedAt(), p.getUpdatedAt());
    }

    private CategoryResponse toCategoryResponse(Category category) {
        if (category == null) {
            return null;
        }
        CategoryResponse parent = toCategoryResponse(category.getParentCategory());
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription(), parent);
    }

    private SupplierResponse toSupplierResponse(Supplier supplier) {
        if (supplier == null) {
            return null;
        }
        AddressResponse addressResponse = null;
        if (supplier.getAddress() != null) {
            Address a = supplier.getAddress();
            addressResponse = new AddressResponse(a.getStreet(), a.getCity(), a.getState(), a.getZipCode(), a.getCountry());
        }
        return new SupplierResponse(supplier.getName(), supplier.getContactEmail(), supplier.getPhone(), addressResponse);
    }

    private DimensionsResponse toDimensionsResponse(Dimensions d) {
        if (d == null) {
            return null;
        }
        return new DimensionsResponse(d.getLength(), d.getWidth(), d.getHeight(), d.getWeight(), d.getUnit());
    }

    private ReviewResponse toReviewResponse(Review r) {
        ReviewerResponse reviewerResponse = r.getReviewer() != null
                ? new ReviewerResponse(r.getReviewer().getName(), r.getReviewer().getEmail())
                : null;
        return new ReviewResponse(r.getId(), r.getRating(), r.getComment(), reviewerResponse, r.getCreatedAt());
    }
}
