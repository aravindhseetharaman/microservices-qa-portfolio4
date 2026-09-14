# Microservices Demo: Product Service + Order Service

Two independent, runnable Spring Boot RESTful microservices with **deeply
nested JSON** request/response bodies and **full CRUD (GET / POST / PUT /
DELETE)** on every primary resource, demonstrating service-to-service
communication:

- **product-service** (port `8081`) — product catalog, categories (with
  self-referencing parent categories), and nested product reviews.
- **order-service** (port `8082`) — creates orders, and when it does, calls
  out to `product-service` over HTTP (via `WebClient`) to look up nested
  product details and reserve stock.

```
┌────────────────┐        HTTP        ┌──────────────────┐
│  order-service   │ ───────────────▶ │  product-service   │
│  :8082           │  GET /products/id │  :8081             │
│                   │  POST /reserve-  │                     │
│                   │       stock       │                     │
└────────────────┘                    └──────────────────┘
```

## Project layout

```
microservices-demo/
├── product-service/     # port 8081
│   └── src/main/java/com/example/productservice/
│       ├── model/        # Product, Category (self-ref), Review, Reviewer,
│       │                 #   Supplier, Address, Dimensions (JPA entities/embeddables)
│       ├── repository/   # ProductRepository, CategoryRepository
│       ├── service/      # ProductService, CategoryService
│       ├── controller/   # ProductController, CategoryController
│       ├── dto/          # Nested request/response DTOs
│       └── exception/    # Custom exceptions + @RestControllerAdvice
├── order-service/       # port 8082
│   └── src/main/java/com/example/orderservice/
│       ├── model/        # Order, OrderItem, Customer, Address, Payment,
│       │                 #   ProductSnapshot (JPA entities/embeddables)
│       ├── repository/
│       ├── service/
│       ├── controller/
│       ├── client/       # ProductClient — WebClient calls to product-service
│       ├── config/
│       ├── dto/
│       └── exception/
├── docker-compose.yml
└── README.md
```

## Nested JSON shapes

### Product (product-service)

`Product` nests a category (which can itself nest a parent category), an
embedded supplier (which nests its own address), embedded dimensions, a
free-form attribute map, a tag array, and an array of reviews (each nesting
a reviewer object):

```json
{
  "id": 1,
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse",
  "price": 29.99,
  "stockQuantity": 98,
  "category": {
    "id": 2,
    "name": "Laptops & Accessories",
    "description": "Laptop peripherals",
    "parentCategory": {
      "id": 1,
      "name": "Electronics",
      "description": "All electronics",
      "parentCategory": null
    }
  },
  "supplier": {
    "name": "Acme Peripherals Ltd",
    "contactEmail": "sales@acmeperipherals.example",
    "phone": "+1-555-0100",
    "address": {
      "street": "123 Supplier Way",
      "city": "Shenzhen",
      "state": "Guangdong",
      "zipCode": "518000",
      "country": "China"
    }
  },
  "dimensions": {
    "length": 10.5,
    "width": 6.2,
    "height": 3.8,
    "weight": 0.09,
    "unit": "cm/kg"
  },
  "attributes": {
    "color": "black",
    "material": "aluminum",
    "connectivity": "bluetooth"
  },
  "tags": ["electronics", "accessories", "bestseller"],
  "reviews": [
    {
      "id": 1,
      "rating": 5,
      "comment": "Excellent battery life",
      "reviewer": { "name": "Jane Doe", "email": "jane@example.com" },
      "createdAt": "2026-09-01T10:15:00Z"
    }
  ],
  "averageRating": 5.0,
  "createdAt": "2026-08-30T09:00:00Z",
  "updatedAt": "2026-09-12T10:00:00Z"
}
```

### Order (order-service)

`Order` nests a customer object, two address objects (shipping and
billing), a payment object, and an array of line items — each item nesting
a product snapshot object captured from product-service at purchase time:

```json
{
  "id": 1,
  "customer": {
    "name": "Jane Doe",
    "email": "jane@example.com",
    "phone": "+1-555-0199"
  },
  "shippingAddress": {
    "street": "456 Main St",
    "city": "Austin",
    "state": "TX",
    "zipCode": "78701",
    "country": "USA"
  },
  "billingAddress": {
    "street": "456 Main St",
    "city": "Austin",
    "state": "TX",
    "zipCode": "78701",
    "country": "USA"
  },
  "payment": {
    "method": "CREDIT_CARD",
    "status": "PAID",
    "transactionId": "TXN-1736700000000",
    "amount": 59.98,
    "processedAt": "2026-09-12T10:05:00Z"
  },
  "status": "CONFIRMED",
  "totalAmount": 59.98,
  "items": [
    {
      "productId": 1,
      "productName": "Wireless Mouse",
      "quantity": 2,
      "unitPrice": 29.99,
      "subtotal": 59.98,
      "productSnapshot": {
        "categoryName": "Laptops & Accessories",
        "supplierName": "Acme Peripherals Ltd"
      }
    }
  ],
  "createdAt": "2026-09-12T10:05:00Z",
  "updatedAt": "2026-09-12T10:05:00Z"
}
```

## Prerequisites

- Java 17+
- Maven 3.6+ (or use Docker instead — see below)

## Running locally (no Docker)

**Terminal 1 — start product-service first:**

```bash
cd product-service
mvn spring-boot:run
```

**Terminal 2 — start order-service:**

```bash
cd order-service
mvn spring-boot:run
```

## Running with Docker Compose

```bash
docker compose up --build
```

## Full walkthrough

**1. Create a parent category:**

```bash
curl -X POST http://localhost:8081/api/v1/categories \
  -H "Content-Type: application/json" \
  -d '{"name": "Electronics", "description": "All electronics"}'
```

**2. Create a nested child category** (`parentCategoryId` from step 1):

```bash
curl -X POST http://localhost:8081/api/v1/categories \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptops & Accessories", "description": "Laptop peripherals", "parentCategoryId": 1}'
```

**3. Create a product with nested supplier/dimensions/attributes/tags:**

```bash
curl -X POST http://localhost:8081/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse",
    "price": 29.99,
    "stockQuantity": 100,
    "categoryId": 2,
    "supplier": {
      "name": "Acme Peripherals Ltd",
      "contactEmail": "sales@acmeperipherals.example",
      "phone": "+1-555-0100",
      "address": {
        "street": "123 Supplier Way",
        "city": "Shenzhen",
        "state": "Guangdong",
        "zipCode": "518000",
        "country": "China"
      }
    },
    "dimensions": { "length": 10.5, "width": 6.2, "height": 3.8, "weight": 0.09, "unit": "cm/kg" },
    "attributes": { "color": "black", "material": "aluminum", "connectivity": "bluetooth" },
    "tags": ["electronics", "accessories", "bestseller"]
  }'
```

**4. Add a nested review:**

```bash
curl -X POST http://localhost:8081/api/v1/products/1/reviews \
  -H "Content-Type: application/json" \
  -d '{"rating": 5, "comment": "Excellent battery life", "reviewer": {"name": "Jane Doe", "email": "jane@example.com"}}'
```

**5. Update the product (PUT — full replace):**

```bash
curl -X PUT http://localhost:8081/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Mouse Pro",
    "description": "Updated ergonomic wireless mouse",
    "price": 34.99,
    "stockQuantity": 100,
    "categoryId": 2,
    "supplier": { "name": "Acme Peripherals Ltd", "contactEmail": "sales@acmeperipherals.example", "phone": "+1-555-0100",
      "address": { "street": "123 Supplier Way", "city": "Shenzhen", "state": "Guangdong", "zipCode": "518000", "country": "China" } },
    "dimensions": { "length": 10.5, "width": 6.2, "height": 3.8, "weight": 0.09, "unit": "cm/kg" },
    "attributes": { "color": "black" },
    "tags": ["electronics"]
  }'
```

**6. Place an order with nested customer/addresses/payment (order-service calls product-service under the hood):**

```bash
curl -X POST http://localhost:8082/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customer": { "name": "Jane Doe", "email": "jane@example.com", "phone": "+1-555-0199" },
    "shippingAddress": { "street": "456 Main St", "city": "Austin", "state": "TX", "zipCode": "78701", "country": "USA" },
    "billingAddress": { "street": "456 Main St", "city": "Austin", "state": "TX", "zipCode": "78701", "country": "USA" },
    "payment": { "method": "CREDIT_CARD" },
    "items": [ { "productId": 1, "quantity": 2 } ]
  }'
```

**7. Update the order's shipping/billing/customer/payment info (PUT):**

```bash
curl -X PUT http://localhost:8082/api/v1/orders/1 \
  -H "Content-Type: application/json" \
  -d '{
    "customer": { "name": "Jane Doe", "email": "jane@example.com", "phone": "+1-555-9999" },
    "shippingAddress": { "street": "789 New Ave", "city": "Austin", "state": "TX", "zipCode": "78702", "country": "USA" },
    "billingAddress": { "street": "456 Main St", "city": "Austin", "state": "TX", "zipCode": "78701", "country": "USA" },
    "payment": { "method": "PAYPAL" }
  }'
```

**8. Delete the order (DELETE):**

```bash
curl -X DELETE http://localhost:8082/api/v1/orders/1
```

## API Reference

### product-service (`:8081`)

| Method | Path                                  | Description                                    |
|--------|----------------------------------------|-------------------------------------------------|
| GET    | `/api/v1/products`                     | List all products (nested category/supplier/etc.)|
| GET    | `/api/v1/products/{id}`                | Get a product by ID                             |
| POST   | `/api/v1/products`                     | Create a product                                |
| PUT    | `/api/v1/products/{id}`                | Replace a product                               |
| DELETE | `/api/v1/products/{id}`                | Delete a product                                |
| GET    | `/api/v1/products/{id}/reviews`        | List a product's nested reviews                 |
| POST   | `/api/v1/products/{id}/reviews`        | Add a nested review (with nested reviewer)      |
| POST   | `/api/v1/products/{id}/reserve-stock`  | Decrement stock (used by order-service)         |
| GET    | `/api/v1/categories`                   | List categories (each with nested parent)       |
| GET    | `/api/v1/categories/{id}`              | Get a category by ID                            |
| POST   | `/api/v1/categories`                   | Create a category (optionally nested under a parent) |
| PUT    | `/api/v1/categories/{id}`              | Update a category                               |
| DELETE | `/api/v1/categories/{id}`              | Delete a category                               |

### order-service (`:8082`)

| Method | Path                          | Description                                  |
|--------|--------------------------------|-----------------------------------------------|
| GET    | `/api/v1/orders`               | List all orders (fully nested)                |
| GET    | `/api/v1/orders/{id}`          | Get an order by ID                            |
| POST   | `/api/v1/orders`               | Create an order (nested customer/addresses/payment/items) |
| PUT    | `/api/v1/orders/{id}`          | Update customer/shipping/billing/payment info |
| DELETE | `/api/v1/orders/{id}`          | Delete an order                               |
| POST   | `/api/v1/orders/{id}/cancel`   | Cancel an order (soft status change)          |

Both services also expose `/actuator/health` and an H2 console at
`/h2-console` for inspecting the in-memory database during development.

## Contract testing (order-service ↔ product-service)

Since order-service depends on product-service's API, the two are covered
by a **consumer-driven contract** using [Pact](https://docs.pact.io/), backed
by a real **Pact Broker** and wired into **CI**, so a breaking change to
product-service's response shape fails a build instead of surfacing as a
runtime error in production.

```
order-service                    Pact Broker                  product-service
┌──────────────────┐   publish   ┌──────────────┐   fetch     ┌───────────────────┐
│ ProductServicePact │ ─────────▶ │  stores the   │ ◀────────  │ ProductServiceProvider│
│ ConsumerTest        │           │  contract,    │            │ PactTest               │
│ (defines what        │           │  tracks which │            │ (seeds state, replays  │
│  order-service        │           │  provider      │            │  the fetched contract  │
│  expects, generates   │           │  versions have │            │  against a real running│
│  the contract)         │           │  verified it)  │  publish  │  instance, publishes    │
│                         │           │               │◀──────────│  pass/fail back)        │
└──────────────────┘             └──────────────┘            └───────────────────┘
```

- **Consumer side:** `order-service/src/test/java/.../contract/ProductServicePactConsumerTest.java`
  defines the two interactions order-service relies on — `GET /api/v1/products/{id}`
  and `POST /api/v1/products/{id}/reserve-stock` — and tests the real
  `ProductClient` against a mock server built from those definitions.
  Running it writes the contract to `target/pacts/order-service-product-service.json`.
- **Provider side:** `product-service/src/test/java/.../contract/ProductServiceProviderPactTest.java`
  starts the real Spring app, seeds the database via `@State` methods to
  match each interaction's precondition, fetches the latest contract from
  the Pact Broker (via `@PactBroker`), replays every interaction against
  the running app, and publishes the pass/fail result back to the broker.
- **`contracts/order-service-product-service.json`** at the repo root is a
  checked-in reference copy of the contract, readable without running any
  tests or standing up a broker — useful in code review.
- **`.github/workflows/contract-tests.yml`** runs the whole flow on every
  push/PR that touches either service: spins up an ephemeral Postgres +
  Pact Broker as CI services, runs the consumer test, publishes the
  contract, then runs the provider test against it.

### Running it locally

Start a broker (same image CI uses):

```bash
docker compose -f docker-compose.pact-broker.yml up -d
# UI at http://localhost:9292
```

Publish the consumer contract, then verify the provider against it:

```bash
cd order-service
mvn test                # generates target/pacts/order-service-product-service.json
mvn pact:publish         # uploads it to the broker at localhost:9292

cd ../product-service
mvn test -Dtest=ProductServiceProviderPactTest   # fetches from the broker and verifies
```

Both the Maven plugin (`pact:publish`) and the provider test read their
broker URL/token/tag from Maven properties with `localhost:9292`/no-auth
defaults, so no flags are needed against the local broker — override with
`-Dpactbroker.url=...` (and `-Dpactbroker.token=...`) to point at a hosted
broker (e.g. [PactFlow](https://pactflow.io/)) instead.

Only the fields order-service's `ProductDto` actually reads (`id`, `name`,
`description`, `price`, `stockQuantity`, `category.*`, `supplier.*`) are
part of the contract — product-service is free to add more nested data
(reviews, dimensions, attributes, tags) without breaking it, since Pact
only asserts on fields the consumer declared.

### CI (GitHub Actions)

`.github/workflows/contract-tests.yml` runs on pushes/PRs to `main` that
touch either service. It:
1. Starts Postgres + a Pact Broker as job services and waits for the
   broker's health check.
2. Runs order-service's consumer test and publishes the resulting contract
   to the broker, tagged with the branch name and the commit SHA as the
   version.
3. Runs product-service's provider test, which fetches the latest contract
   tagged for that branch, verifies it, and publishes the result back to
   the broker.
4. Uploads the generated contract as a build artifact for inspection.

In a real team setup you'd typically add a second job (or a
[`can-i-deploy`](https://docs.pact.io/pact_broker/can_i_deploy) step) that
blocks deployment of either service unless the broker confirms the specific
versions being deployed have a verified contract between them — useful once
these two services deploy independently rather than always together.

## Design notes

- **Nested structures, mapped explicitly.** Rather than serializing JPA
  entities directly (which risks lazy-loading and circular-reference
  issues), every nested shape is mirrored by a dedicated request/response
  DTO, and the service layer explicitly maps entity ↔ DTO. This keeps the
  wire format stable even if the persistence model changes.
- **JPA nesting techniques used:** `@Embeddable`/`@Embedded` for value
  objects nested inside other value objects (e.g. `Supplier` embeds
  `Address`), `@AttributeOverrides` where the same embeddable type appears
  twice on one entity (Order's `shippingAddress` and `billingAddress`),
  a self-referencing `@ManyToOne` for `Category`'s parent, `@ElementCollection`
  for the attribute map and tag array, and `@OneToMany` for the nested
  review/order-item arrays.
- **Order update semantics.** `PUT /orders/{id}` updates customer contact
  info, addresses, and payment method, but not line items — changing items
  would require re-reserving stock on product-service, which is out of
  scope for a simple field update. Recreate the order for item changes.
- **Price snapshotting.** Each `OrderItem` stores the product name/price/
  category/supplier as they were at time of purchase, so historical orders
  aren't affected by later changes in product-service.

## Next steps for a production setup

- Add a service registry (Eureka/Consul) instead of hardcoded URLs.
- Add an API gateway (Spring Cloud Gateway) as a single entry point.
- Add Resilience4j circuit breakers around the `ProductClient` calls.
- Swap H2 for PostgreSQL/MySQL per service.
- Add distributed tracing (Micrometer + Zipkin/Jaeger) across the two calls.
