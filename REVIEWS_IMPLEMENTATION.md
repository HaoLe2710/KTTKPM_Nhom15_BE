# REVIEWS Module Implementation Summary

## Overview

The **REVIEWS Module** has been fully implemented following the established Clean Architecture + CQRS + Event-Driven patterns used across the KTTKPM_Nhom15_BE project. The module enables users to submit product reviews after completing orders.

---

## Database Schema

```sql
-- From V1__init_schema.sql (already in migrations)
CREATE TABLE reviews (
    id VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id VARCHAR(255) NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content TEXT,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    UNIQUE (user_id, product_id)  -- One review per user per product
);
```

---

## Implemented Components

### 1. DOMAIN LAYER (`reviews/domain/`)

#### Models
- **[Review.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/domain/models/Review.java)**
  - Core domain entity
  - Fields: `id`, `userId`, `productId`, `orderId`, `rating` (1-5), `content`, `createdAt`
  - Built with Lombok `@Builder` for fluent construction

#### Repository Interface
- **[ReviewRepository.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/domain/repositories/ReviewRepository.java)**
  - Contract for persistence operations
  - Methods: `save()`, `findById()`, `findByUserIdAndProductId()`, `findByProductId()`, `findByUserId()`, `findByOrderId()`

#### Domain Exceptions
- **[InvalidRatingException.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/domain/exceptions/InvalidRatingException.java)**
  - Thrown when rating is not in range 1-5
  
- **[ReviewAlreadyExistsException.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/domain/exceptions/ReviewAlreadyExistsException.java)**
  - Thrown when user already reviewed this product (unique constraint violation)
  
- **[OrderNotFoundException.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/domain/exceptions/OrderNotFoundException.java)**
  - Thrown when order doesn't exist
  
- **[OrderNotCompletedException.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/domain/exceptions/OrderNotCompletedException.java)**
  - Thrown when order hasn't reached COMPLETED status yet

---

### 2. INFRASTRUCTURE LAYER (`reviews/infrastructure/`)

#### JPA Entity
- **[ReviewJpaEntity.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/infrastructure/persistence/entities/ReviewJpaEntity.java)** (pre-existing)
  - ORM mapping to `reviews` table
  - Uses `@CreationTimestamp` for automatic timestamp generation
  - Unique constraint on (userId, productId)

#### Repositories
- **[JpaReviewRepository.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/infrastructure/persistence/repositories/JpaReviewRepository.java)**
  - Spring Data JPA repository interface
  - Query methods for various find operations
  
- **[ReviewRepositoryImpl.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/infrastructure/persistence/repositories/ReviewRepositoryImpl.java)**
  - Implements domain `ReviewRepository` interface
  - Converts between `ReviewJpaEntity` ↔ `Review` domain model
  - Marked with `@Repository` for component scanning

#### Mapper
- **[ReviewDataMapper.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/infrastructure/persistence/mappers/ReviewDataMapper.java)** (pre-existing)
  - MapStruct interface for automatic entity ↔ domain mapping
  - Compile-time generated implementation

---

### 3. APPLICATION LAYER (`reviews/application/`)

#### Commands
- **[CreateReviewCommand.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/application/commands/CreateReviewCommand.java)**
  - Input DTO from presentation layer
  - Fields: `userId`, `productId`, `orderId`, `rating`, `content`

#### DTOs
- **[ReviewDTO.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/application/dto/ReviewDTO.java)**
  - Transfer object for general review data
  - Includes `createdAt` timestamp

#### Results
- **[CreateReviewResult.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/application/results/CreateReviewResult.java)**
  - Output DTO for successful review creation
  - Matches HTTP response structure (201 CREATED)

#### Events
- **[ReviewCreatedEvent.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/application/events/ReviewCreatedEvent.java)**
  - Application event published when review successfully created
  - Contains complete `Review` domain model
  - Other modules can `@EventListener` to react

#### Interfaces (Inter-Module Contracts)
- **[OrderFacade.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/application/interfaces/OrderFacade.java)**
  - Contract for verifying orders in Orders module
  - Method: `verifyOrderForReview(orderId, userId)`
  - Verifies: order exists, belongs to user, is COMPLETED

#### Use Cases
- **[CreateReviewUseCase.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/application/usecases/CreateReviewUseCase.java)**
  - Main business orchestrator
  - Marked with `@Service` and `@Transactional` for atomicity
  - Execution flow:
    1. Validate rating (1-5 range)
    2. Call `OrderFacade.verifyOrderForReview()` to verify order is valid & COMPLETED
    3. Check for existing review (unique constraint on userId + productId)
    4. Create Review domain model
    5. Persist via `ReviewRepository.save()`
    6. Publish `ReviewCreatedEvent` via Spring's `ApplicationEventPublisher`
    7. Return `CreateReviewResult` with review details

---

### 4. PRESENTATION LAYER (`reviews/presentation/`)

#### Controllers
- **[ReviewController.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/presentation/controllers/ReviewController.java)**
  - REST endpoint: `POST /api/v1/reviews`
  - Receives `CreateReviewRequest`, builds `CreateReviewCommand`, invokes `CreateReviewUseCase`
  - Returns HTTP 201 CREATED with `CreateReviewResult` body
  - Exception handlers for all domain exceptions:
    - `InvalidRatingException` → 400 BAD_REQUEST
    - `OrderNotFoundException` → 404 NOT_FOUND
    - `OrderNotCompletedException` → 409 CONFLICT
    - `ReviewAlreadyExistsException` → 409 CONFLICT

#### Requests
- **[CreateReviewRequest.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/presentation/requests/CreateReviewRequest.java)**
  - Request DTO for POST /api/v1/reviews
  - Includes Jakarta validation annotations
  - Fields: `userId`, `productId`, `orderId`, `rating` (1-5), `content`

---

### 5. ORDERS MODULE EXTENSION (`orders/application/facades/`)

#### Facade Implementation (Minimal Extension to Orders)
- **[OrderFacadeForReviewsImpl.java](src/main/java/fit/iuh/kttkpm_nhom15_be/orders/application/facades/OrderFacadeForReviewsImpl.java)**
  - Implements `reviews.OrderFacade` interface
  - Marked with `@Component` for Spring injection into Reviews module
  - Depends on `OrderRepository` from orders domain
  - Verification logic:
    1. Find order by ID (throws `OrderNotFoundException` if not found)
    2. Verify order belongs to user (throws `OrderNotFoundException` if mismatch)
    3. Verify order status is COMPLETED (throws `OrderNotCompletedException` otherwise)

---

## Execution Flow: Create Review

```
Client
  │
  ├─ POST /api/v1/reviews
  │  Request body: CreateReviewRequest {userId, productId, orderId, rating, content}
  │
  ↓
ReviewController.createReview()
  │
  ├─ Validate request (Jakarta validation)
  ├─ Build CreateReviewCommand from request
  │
  ↓
CreateReviewUseCase.execute(command)
  │
  ├─ [1] Validate rating: 1 ≤ rating ≤ 5
  │       ✗ throw InvalidRatingException
  │
  ├─ [2] Call OrderFacade.verifyOrderForReview(orderId, userId)
  │       │
  │       ├─ OrderFacadeForReviewsImpl.verifyOrderForReview()
  │       │  ├─ OrderRepository.findById(orderId)
  │       │  │  ✗ throw OrderNotFoundException (not found)
  │       │  ├─ Verify order.userId == userId
  │       │  │  ✗ throw OrderNotFoundException (unauthorized)
  │       │  ├─ Verify order.status == COMPLETED
  │       │     ✗ throw OrderNotCompletedException
  │       │  ✓ pass
  │       │
  │       ↓ validation complete
  │
  ├─ [3] ReviewRepository.findByUserIdAndProductId(userId, productId)
  │       ✓ not found (good, first review)
  │       ✗ throw ReviewAlreadyExistsException
  │
  ├─ [4] Create Review domain model
  │       Review.builder()
  │         .userId(command.userId)
  │         .productId(command.productId)
  │         .orderId(command.orderId)
  │         .rating(command.rating)
  │         .content(command.content)
  │         .build()
  │
  ├─ [5] ReviewRepository.save(review)
  │       │
  │       ├─ ReviewRepositoryImpl.save()
  │       │  ├─ ReviewDataMapper.toJpaEntity(review)
  │       │  ├─ JpaReviewRepository.save(reviewJpaEntity)
  │       │  └─ ReviewDataMapper.toDomainModel(savedEntity)
  │       │
  │       ↓ Review.id generated (UUID), createdAt auto-set (CURRENT_TIMESTAMP)
  │       ↓ savedReview returned
  │
  ├─ [6] ApplicationEventPublisher.publishEvent(ReviewCreatedEvent)
  │       (Async listeners can subscribe, e.g., to update product ratings, send emails, etc.)
  │
  ├─ [7] Build and return CreateReviewResult
  │       {id, userId, productId, orderId, rating, content, createdAt}
  │
  ↓
ReviewController.createReview() returns
  HTTP 201 CREATED
  Body: CreateReviewResult (JSON)
  │
  ↓
Client receives response
```

---

## Key Design Decisions

1. **Unique Constraint (userId, productId)**
   - One review per user per product
   - Enforced at both DB and application layer

2. **Order Verification**
   - Reviews only allowed for COMPLETED orders
   - Proves user owns the product (purchase proof)
   - Prevents fake reviews

3. **Immutable Reviews**
   - Only `created_at`, no `updated_at`
   - Once created, reviews cannot be edited
   - Ensures review integrity

4. **Event Publishing**
   - `ReviewCreatedEvent` published after save
   - Allows catalog module to update product ratings asynchronously
   - Loosely couples modules

5. **MapStruct Mapping**
   - Compile-time type-safe conversion
   - Automatic field matching by name
   - Supports custom mappers for complex fields if needed

6. **Exception Hierarchy**
   - Domain exceptions extend `RuntimeException`
   - Not checked exceptions (Spring convention)
   - Controller catches and maps to HTTP status codes

---

## Dependencies

### Injected via Dependency Injection
- `ReviewRepository` - Interface from domain, impl from infrastructure
- `OrderFacade` - Interface from reviews app, impl from orders module
- `ApplicationEventPublisher` - Provided by Spring Framework

### Spring Annotations Used
- `@Entity`, `@Table` - JPA mapping
- `@Repository` - Component stereotype for repository impls
- `@Service` - Component stereotype for use cases
- `@Component` - Generic component (facade impl)
- `@Mapper` - MapStruct code generation
- `@Transactional` - Transaction management
- `@RestController`, `@RequestMapping`, `@PostMapping` - REST endpoints
- `@NotBlank`, `@Min`, `@Max` - Jakarta validation
- `@RequiredArgsConstructor` - Lombok constructor generation
- `@Getter`, `@Setter`, `@Builder` - Lombok data generation

---

## REST API Endpoint

### Create Review
```http
POST /api/v1/reviews
Content-Type: application/json

{
  "userId": "user-uuid-123",
  "productId": "product-uuid-456",
  "orderId": "order-uuid-789",
  "rating": 5,
  "content": "Excellent product, highly recommended!"
}
```

**Response: 201 CREATED**
```json
{
  "id": "review-uuid-001",
  "userId": "user-uuid-123",
  "productId": "product-uuid-456",
  "orderId": "order-uuid-789",
  "rating": 5,
  "content": "Excellent product, highly recommended!",
  "createdAt": "2026-03-25T14:30:00"
}
```

**Error Responses**
- `400 BAD_REQUEST` - Invalid rating (< 1 or > 5)
- `404 NOT_FOUND` - Order doesn't exist
- `409 CONFLICT` - Order not completed OR review already exists

---

## Testing Checklist (For QA)

- [ ] Create review with valid data → 201 CREATED
- [ ] Rating 0 → 400 BAD_REQUEST
- [ ] Rating 6 → 400 BAD_REQUEST
- [ ] Non-existent order → 404 NOT_FOUND
- [ ] Order not COMPLETED → 409 CONFLICT
- [ ] Duplicate review (same user + product) → 409 CONFLICT
- [ ] Verify `ReviewCreatedEvent` is published
- [ ] Verify unique constraint in DB

---

## Future Enhancements (Out of Scope)

1. **GET endpoints**
   - Get review by ID
   - List reviews for product (paginated)
   - List reviews by user

2. **Update/Delete**
   - Edit review content (but not rating)
   - Delete review (with permission checks)

3. **Product Ratings**
   - Calculate average rating from reviews
   - Update product average rating on ReviewCreatedEvent

4. **Analytics**
   - Track most reviewed products
   - User review count statistics

5. **Moderation**
   - Flag inappropriate reviews
   - Admin approval workflow

---

## File Structure Summary

```
reviews/
├─ domain/
│  ├─ models/
│  │  └─ Review.java
│  ├─ repositories/
│  │  └─ ReviewRepository.java
│  └─ exceptions/
│     ├─ InvalidRatingException.java
│     ├─ ReviewAlreadyExistsException.java
│     ├─ OrderNotFoundException.java
│     └─ OrderNotCompletedException.java
├─ infrastructure/
│  ├─ persistence/
│  │  ├─ entities/
│  │  │  └─ ReviewJpaEntity.java (pre-existing)
│  │  ├─ repositories/
│  │  │  ├─ JpaReviewRepository.java
│  │  │  └─ ReviewRepositoryImpl.java
│  │  └─ mappers/
│  │     └─ ReviewDataMapper.java (pre-existing)
├─ application/
│  ├─ commands/
│  │  └─ CreateReviewCommand.java
│  ├─ dto/
│  │  └─ ReviewDTO.java
│  ├─ results/
│  │  └─ CreateReviewResult.java
│  ├─ events/
│  │  └─ ReviewCreatedEvent.java
│  ├─ interfaces/
│  │  └─ OrderFacade.java
│  └─ usecases/
│     └─ CreateReviewUseCase.java
└─ presentation/
   ├─ controllers/
   │  └─ ReviewController.java
   └─ requests/
      └─ CreateReviewRequest.java

orders/ (minimal extension)
└─ application/
   └─ facades/
      └─ OrderFacadeForReviewsImpl.java
```

---

## Compilation Status

✅ **No errors found in reviews module**
✅ **No errors found in orders extension**
✅ All 4 layers properly implemented
✅ All dependencies resolved
✅ Code follows established patterns
