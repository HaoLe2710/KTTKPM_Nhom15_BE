# System Architecture - KTTKPM_Nhom15_BE

## Project Overview

**KTTKPM_Nhom15_BE** is a Beauty Product E-commerce backend built with **Spring Boot** following **Clean Architecture + CQRS + Event-Driven Architecture** patterns.

### Core Technology Stack
- **Framework**: Spring Boot 3.x, Spring Data JPA, Spring Modulith
- **Data Mapping**: MapStruct (annotation-based entity ↔ domain converters)
- **ORM**: Hibernate with Jakarta Persistence (JPA)
- **Database**: PostgreSQL with JSONB support
- **Migrations**: FlywayDB
- **Build**: Maven
- **Serialization**: Lombok (reduce boilerplate)

---

## System Architecture

### Layered Architecture Per Module

Each major module (Users, Catalog, Carts, Orders, Payments, Chat, Reviews) follows a **4-layer architecture**:

```
presentation/
  ├─ controllers/      → @RestController - REST endpoints
  └─ requests/        → Request DTOs (command-like objects)

application/
  ├─ usecases/        → @Service - Business orchestration
  ├─ commands/        → Command objects (CQS pattern)
  ├─ results/         → Result DTOs (queries return these)
  ├─ dto/            → Data Transfer Objects
  ├─ events/         → Application events (@EventListener)
  └─ interfaces/     → Facades (inter-module contracts)

domain/
  ├─ models/         → Core business entities (Lombok @Builder)
  ├─ repositories/   → Domain repository interfaces
  └─ exceptions/     → Domain-specific exceptions

infrastructure/
  └─ persistence/
      ├─ entities/   → JpaEntity - ORM mapping (@Entity, @Table, @ManyToOne, etc.)
      ├─ repositories/ → Implementation of domain repository interfaces
      └─ mappers/    → MapStruct mappers (Entity ↔ Domain)
```

### Module Responsibilities

#### 1. USERS Module
- Manages user accounts with roles: ADMIN, CUSTOMER, STAFF
- Stores user profile info and delivery addresses

**Key Classes**:
- `domain/models/User.java` - Core user entity with List<Address>
- `domain/models/UserRole.java` - Enum: ADMIN, CUSTOMER, STAFF
- `infrastructure/persistence/entities/UserJpaEntity.java`
- `infrastructure/persistence/mappers/UserDataMapper.java`

#### 2. CATALOG Module
- Product catalog: Products, Variants, Options, Media
- Inventory management: Stock quantities at variant level
- **Exposes CatalogFacade** for inter-module use

**Key Classes**:
- `domain/models/Product.java` - Main product entity
- `domain/models/Variant.java` - Each product variant with SKU, price, stock_quantity
- `domain/models/Option.java` - Product attributes (Color, Size, etc.)
- `domain/models/Media.java` - Product images/videos
- `application/interfaces/CatalogFacade.java` - Public contract:
  - `validateAndGetSnapshots(cartItems)` → List<VariantSnapshot>
  - `restoreStock(items)` - Called when order cancelled
  - `checkAvailabilityAndPrice(variantId, quantity)` → VariantInfoDTO

**Data Flow**:
```
PlaceOrder
  → CatalogFacade.validateAndGetSnapshots(cartItems)
    → Verifies stock availability
    → Returns snapshot (id, sku, productName, price, attributes...)
    → Used to create OrderItems with historical data
```

#### 3. CARTS Module
- Shopping cart management: Cart + CartItem
- **Exposes CartFacade** for orders

**Key Classes**:
- `domain/models/Cart.java` - Contains List<CartItem>
- `domain/models/CartItem.java` - variant_id, quantity, unit_price
- `application/interfaces/CartFacade.java`:
  - `getActiveCart(userId)` → CartDTO
  - Throws `EmptyCartException` if cart is empty
- `application/dto/CartDTO.java` - Transfer object with items

**Key Insight**: CartItem references Variant, not Product directly. Creates cart association with User.

#### 4. ORDERS Module ⭐ (Primary Flow)
- Creates orders from carts with full order lifecycle
- State Pattern: OrderStatus enum (CREATED → CONFIRMED → SHIPPING → COMPLETED/CANCELLED)
- Publishes events for downstream processing

**Key Classes**:
- `domain/models/Order.java` - Main order with 15+ fields
- `domain/models/OrderItem.java` - Snapshot of variant at order time (sku, name, options_snapshot, imageUrl, quantity, unitPrice, lineTotal)
- `domain/models/OrderStatus.java` - Enum (CREATED, CONFIRMED, SHIPPING, COMPLETED, CANCELLED)
- `domain/models/PaymentStatus.java` - Enum (UNPAID, PAID)
- `domain/models/ShippingMode.java` - Enum (HCM_FLAT, PROVIDER_API)
- `application/usecases/PlaceOrderUseCase.java` - Main orchestrator
- `application/commands/PlaceOrderCommand.java` - Input DTO
- `application/results/PlaceOrderResult.java` - Output DTO
- `presentation/controllers/OrderController.java` - REST endpoints:
  - `POST /api/v1/orders` → placeOrder
  - `POST /api/v1/orders/{orderId}/cancel` → cancelOrder
- `application/events/OrderPlacedEvent.java` - Fired when order created
- `domain/repositories/OrderRepository.java` - Domain interface
- `infrastructure/persistence/repositories/OrderRepositoryImpl.java` - Persistence impl
- `infrastructure/persistence/mappers/OrderDataMapper.java` - Entity ↔ Domain

**PlaceOrder Sequence** (Key Execution Flow):
```
1. Client POST /api/v1/orders (PlaceOrderRequest)
2. OrderController validates and builds PlaceOrderCommand
3. PlaceOrderUseCase.execute(command)
   a. CartFacade.getActiveCart(userId) - Get cart or throw EmptyCartException
   b. CatalogFacade.validateAndGetSnapshots(cartItems) - Verify stock
   c. Build List<OrderItem> from CartItem + VariantSnapshot
   d. Calculate totals: subtotal, discount, shipping, total
   e. Create Order (status=CREATED)
   f. OrderRepository.save(order) → JpaOrderRepository.save(OrderJpaEntity)
   g. ApplicationEventPublisher.publishEvent(OrderPlacedEvent)
4. Return PlaceOrderResult with order details (201 CREATED)
```

**Data Snapshots**: Order stores historical/snapshot data at order time:
- ProductName, SKU (from variant)
- OptionsSnapshot (product attributes as JSONB)
- ImageUrl, UnitPrice (prices don't change if product updated)
- Quantity ordered

#### 5. PAYMENTS Module
- Payment transaction records
- Supports multiple providers: VNPAY, SEPAY, COD

**Key Classes**:
- `domain/models/PaymentTransaction.java` - Payment record
- `domain/models/PaymentProvider.java` - Enum (VNPAY, SEPAY, COD)
- `domain/models/PaymentMethod.java` - Enum
- `domain/models/PaymentTxnStatus.java` - Enum (PENDING, SUCCESS, FAILED)
- `infrastructure/persistence/entities/PaymentTransactionJpaEntity.java` - Stores raw_payload as JSONB

#### 6. CHAT Module
- Chat room between customer and staff
- Messages in chat room

**Key Classes**:
- `domain/models/ChatRoom.java` - user_id (customer), staff_id (staff/admin)
- `domain/models/ChatMessage.java` - room_id, sender_id, content, timestamp
- `infrastructure/persistence/entities/ChatRoomJpaEntity.java` - Has @OneToMany List<ChatMessageJpaEntity>
- `infrastructure/persistence/mappers/ChatDataMapper.java` - @AfterMapping links messages to room

#### 7. REVIEWS Module ⭐ (To Implement According to Spec)
- User reviews for products after purchase
- Links review to Order (proof of purchase)
- Rating 1-5, optional review content
- Unique constraint: (user_id, product_id)

**Database Schema** (from V1__init_schema.sql):
```sql
CREATE TABLE reviews (
    id VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id VARCHAR(255) NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content TEXT,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

---

## Communication Flows

### 1. Place Order (Primary Flow)

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ POST /api/v1/orders
       ↓
┌──────────────────────────────────────────────────────────────────────┐
│ OrderController.placeOrder(PlaceOrderRequest)                        │
│  - Validates input                                                    │
│  - Builds PlaceOrderCommand                                           │
└──────────┬───────────────────────────────────────────────────────────┘
           │
           ↓
┌──────────────────────────────────────────────────────────────────────┐
│ PlaceOrderUseCase.execute(PlaceOrderCommand)                         │
│                                                                       │
│ Step 1: Retrieve Cart                                                │
│  ├─ CartFacade.getActiveCart(userId)                                │
│  └─ Returns CartDTO or throws EmptyCartException                    │
│                                                                       │
│ Step 2: Validate Inventory & Get Snapshots                          │
│  ├─ CatalogFacade.validateAndGetSnapshots(cartItems)                │
│  └─ Returns List<VariantSnapshot>                                   │
│     (sku, productName, price, attributes, imageUrl, stock check)   │
│                                                                       │
│ Step 3: Build OrderItems                                            │
│  ├─ For each CartItem: combine with VariantSnapshot                │
│  └─ Create OrderItem with: variantId, sku, name, options, qty...  │
│                                                                       │
│ Step 4: Calculate Totals                                            │
│  ├─ Subtotal = sum(lineTotal per item)                             │
│  ├─ Discount = 0 (placeholder for coupons)                         │
│  ├─ Shipping = from command.shippingFee                            │
│  └─ Total = subtotal - discount + shipping                        │
│                                                                       │
│ Step 5: Create Order Domain Model                                   │
│  ├─ status = CREATED (not CONFIRMED yet)                           │
│  ├─ paymentStatus = UNPAID                                         │
│  └─ paymentMethod = from command                                   │
│                                                                       │
│ Step 6: Persist Order                                               │
│  ├─ OrderRepository.save(order)                                     │
│  ├─ OrderRepositoryImpl converts: Order → OrderJpaEntity             │
│  └─ OrderDataMapper links OrderItems to Order, saves via JPA       │
│                                                                       │
│ Step 7: Publish Event                                               │
│  └─ ApplicationEventPublisher.publishEvent(OrderPlacedEvent)        │
└────────────────────┬──────────────────────────────────────────────┘
                     │ PlaceOrderResult (order summary)
                     ↓
          OrderController returns
              HTTP 201 CREATED
                     │
                     ↓
        ┌─────────────────────────────┐
        │  OrderPlacedEvent published │
        │  (async @EventListener)     │
        │  - Update analytics?        │
        │  - Notify inventory?        │
        │  - Email confirmation?      │
        └─────────────────────────────┘
```

### 2. Inter-Module Communication Pattern

**Facade Pattern** - Modules expose interfaces for cross-module calls:

```
┌──────────────┐          Injected via       ┌────────────────────┐
│ Orders       │ @RequiredArgsConstructor    │ Catalog            │
│ UseCase      │──────────────────────────→ │ CatalogFacade      │
│              │                             │ (application)      │
└──────────────┘                             └────────────────────┘
     │                                              │
     │ validateAndGetSnapshots(cartItems)         │
     │────────────────────────────────────────────→ Checks variant stock
     ←────────────────────────────────────────────
     │                   VariantSnapshot list
     │


┌──────────────┐          Injected via       ┌────────────────────┐
│ Orders       │ @RequiredArgsConstructor    │ Carts              │
│ UseCase      │──────────────────────────→ │ CartFacade         │
│              │                             │ (application)      │
└──────────────┘                             └────────────────────┘
     │                                              │
     │ getActiveCart(userId)                       │
     │────────────────────────────────────────────→ Retrieves active cart
     ←────────────────────────────────────────────
     │                   CartDTO
```

**Event-Driven** - Modules publish events others listen to:

```
PlaceOrderUseCase
  ├─ publishEvent(OrderPlacedEvent)     [Any listener can subscribe]
  │
  └─ Async listeners (if any):
      ├─ @EventListener OrderPlacedEvent
      ├─ Update inventory stats?
      ├─ Send email?
      └─ Analytics?
```

---

## Design Patterns Used

### 1. **Repository Pattern**
- **Domain defines contract**: `domain/repositories/OrderRepository.java` (interface)
  ```
  public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String id);
    List<Order> findByUserId(String userId);
  }
  ```
- **Infrastructure implements**: `infrastructure/repositories/OrderRepositoryImpl.java`
  - Depends on JPA repository, data mapper
  - Converts domain ↔ entity types

### 2. **Data Mapper Pattern** (MapStruct)
- **Entity ↔ Domain conversion**:
  ```java
  @Mapper(componentModel = "spring")
  public interface OrderDataMapper {
    OrderJpaEntity toJpaEntity(Order domain);
    Order toDomainModel(OrderJpaEntity entity);
    
    @AfterMapping
    default void linkItems(@MappingTarget OrderJpaEntity orderJpa) {
      if (orderJpa.getItems() != null) 
        orderJpa.getItems().forEach(i -> i.setOrder(orderJpa));
    }
  }
  ```
- Benefits: Separate concerns, testable mappers, generated code

### 3. **Facade Pattern**
- Multi-module coordination via well-defined interfaces
- Examples: `CatalogFacade`, `CartFacade`
- Reduces coupling between modules

### 4. **Use Case Pattern** (Application Services)
- **One service per use case**: `PlaceOrderUseCase`, `CancelOrderUseCase`
- Orchestrates: domain logic + repository + inter-module calls + events
- Input: Command object, Output: Result DTO
- `@Transactional` for atomicity

### 5. **State Pattern** (Domain Models)
- Enums define valid states: `OrderStatus`, `PaymentStatus`, `ShippingMode`
- Use cases validate state transitions
- Example: Order can only be cancelled if status ∈ {CREATED, CONFIRMED}

### 6. **Event-Driven Architecture**
- Domain/application events published after state changes
- Listeners can react asynchronously
- Decouples modules (e.g., notification, analytics services)

### 7. **Builder Pattern** (Lombok)
- `@Builder` generates builder pattern for creating complex objects
- Used in domain models and DTOs

---

## Data Model Highlights

### Order-OrderItem Relationship
```java
@Entity
class OrderJpaEntity {
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItemJpaEntity> items;  // Cascade delete
}

@Entity
class OrderItemJpaEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private OrderJpaEntity order;
  
  private String variantId;  // Foreign key to catalog
  private String sku;        // Snapshot at order time
  private String name;       // Snapshot at order time
  private Map<String, Object> optionsSnapshot;  // JSONB: {color: "red", size: "M"}
  private String imageUrl;   // Snapshot at order time
  private int quantity;
  private BigDecimal unitPrice;  // Price at order time
  private BigDecimal lineTotal;  // quantity * unitPrice
}
```

**Key**: OrderItem stores snapshots (immutable historical data) at order time. If product updates later, existing orders retain original data.

### Cart-CartItem Relationship
```java
@Entity
class CartJpaEntity {
  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
  private List<CartItemJpaEntity> items;
}

@Entity
class CartItemJpaEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id")
  private CartJpaEntity cart;
  
  private String variantId;  // Reference to live catalog
  private int quantity;
  private BigDecimal unitPrice;  // Current price (not snapshot)
}
```

**Key**: CartItem references live variant; prices update in real-time.

---

## Dependency Injection & Spring Bean Lifecycle

### Typical Service Being Injected
```java
@Service
@RequiredArgsConstructor  // Lombok generates all-args constructor
public class PlaceOrderUseCase {
  private final OrderRepository orderRepository;      // Domain interface
  private final CartFacade cartFacade;               // Inter-module
  private final CatalogFacade catalogFacade;         // Inter-module
  private final ApplicationEventPublisher eventPublisher;  // Spring-provided
  
  @Transactional
  public PlaceOrderResult execute(PlaceOrderCommand cmd) { ... }
}
```

### Spring Autowiring Flow
1. Spring detects `@Service` annotated class
2. Spring inspects constructor parameters
3. Spring resolves each dependency:
   - `OrderRepository` → finds `OrderRepositoryImpl` (implements interface, marked `@Repository`)
   - `CartFacade` → finds implementation (e.g., `CartFacadeImpl` or direct impl)
   - `CatalogFacade` → finds implementation
   - `ApplicationEventPublisher` → Spring-provided singleton
4. Creates instance with beans passed to constructor

---

## Database Structure

Each module stores data in dedicated tables:

### USERS Module
- `users` - User accounts (id, email, phone, password, role, etc.)
- `addresses` - User addresses (user_id FK, full_name, address, city, etc.)

### CATALOG Module
- `product_types` - Categories
- `products` - Main products (id, type_id FK, name, slug, description_md, etc.)
- `variants` - Product variants (id, product_id FK, sku, price, stock_quantity)
- `options` - Option templates (Color, Size, etc.)
- `option_values` - Actual values (Red, Blue, Small, Large, etc.)
- `variant_options` - Association between variants and option values
- `media` - Product images/videos

### CARTS Module
- `carts` - Carts (id, user_id FK, status)
- `cart_items` - Items in cart (id, cart_id FK, variant_id FK, quantity, unit_price)

### ORDERS Module
- `orders` - Orders (id, order_no, user_id FK, subtotal, discount, shipping, total, status, payment_status, shipping_address, etc.)
- `order_items` - Items in order (id, order_id FK, variant_id, sku, name, options_snapshot JSONB, quantity, unit_price, line_total)

### PAYMENTS Module
- `payment_transactions` - Payment records (id, order_id FK, provider, method, amount, status, txn_ref, raw_payload JSONB)

### CHAT Module
- `chat_rooms` - Chat rooms (id, user_id FK UNIQUE, staff_id FK)
- `chat_messages` - Messages (id, room_id FK, sender_id FK, content)

### REVIEWS Module
- `reviews` - Product reviews (id, user_id FK, product_id FK, order_id FK, rating INT [1-5], content TEXT)
  - Unique constraint: (user_id, product_id)

---

## REST API Endpoints (Examples)

### Orders Module
- **POST /api/v1/orders** - Place order
  - Request: `PlaceOrderRequest` (userId, shipFullName, shipAddress, shippingMode, paymentMethod, etc.)
  - Response: `PlaceOrderResult` (201 CREATED)

- **POST /api/v1/orders/{orderId}/cancel** - Cancel order
  - Request: `CancelOrderRequest` (reason string)
  - Response: `CancelOrderResult` (200 OK)

### Reviews Module (To Implement)
- **POST /api/v1/reviews** - Create review
  - Request: `CreateReviewRequest` (userId, productId, orderId, rating, content)
  - Response: `ReviewResult` (201 CREATED)

- **GET /api/v1/reviews/{reviewId}** - Get review by ID
- **GET /api/v1/reviews/product/{productId}** - List reviews for product
- **GET /api/v1/reviews/user/{userId}** - List reviews by user

---

## Execution Flow Comparison

### Current (Orders)
```
PlaceOrder Request
  → OrderController
    → PlaceOrderUseCase
      → CartFacade.getActiveCart()
      → CatalogFacade.validateAndGetSnapshots()
      → OrderRepository.save()
      → ApplicationEventPublisher (OrderPlacedEvent)
  → Response (201 CREATED)
```

### Expected (Reviews - Following Same Pattern)
```
CreateReview Request
  → ReviewController
    → CreateReviewUseCase
      → OrderFacade.verifyOrderForReview()  [Verify user owns order, order completed]
      → ReviewRepository.save()
      → ApplicationEventPublisher (ReviewCreatedEvent)
  → Response (201 CREATED)
```

---

## Summary of Key Architectural Decisions

1. **Layered per module** - Clear separation: presentation, application, domain, infrastructure
2. **Interface-driven repositories** - Invert dependency: domain defines interface, infra implements
3. **MapStruct for mappings** - Type-safe, compile-time checked entity ↔ domain conversions
4. **Facades for inter-module** - Reduce coupling, clear contracts between modules
5. **Events for async deps** - Decouple modules that need to react to state changes
6. **Snapshots in orders** - Orders store historical data, don't rely on live product data
7. **Use cases as transaction boundaries** - Each use case is atomic unit with `@Transactional`
8. **Builder pattern for DTOs** - Readable, testable construction
9. **Enums for state** - Type safety for domain states
10. **JSONB for flexible data** - OptionsSnapshot, ShippingMeta stored as JSONB for schema flexibility
