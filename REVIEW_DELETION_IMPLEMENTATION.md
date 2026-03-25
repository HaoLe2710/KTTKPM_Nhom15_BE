# Review Deletion Function - Implementation Details

## Overview

Implemented a complete **Delete Review** feature following the same Clean Architecture patterns as the Create Review functionality. The deletion function includes authorization checks, event publishing, and proper exception handling.

---

## Components Implemented

### 1. DOMAIN LAYER

#### New Exception Classes

**[ReviewNotFoundException.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/domain/exceptions/ReviewNotFoundException.java)**
- Thrown when a review with given ID doesn't exist
- HTTP Response: 404 NOT_FOUND

**[UnauthorizedReviewAccessException.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/domain/exceptions/UnauthorizedReviewAccessException.java)**
- Thrown when a user attempts to delete a review they don't own
- HTTP Response: 403 FORBIDDEN
- Prevents unauthorized deletion (authorization check)

#### Repository Interface Update

**[ReviewRepository.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/domain/repositories/ReviewRepository.java)** - UPDATED
- Added methods:
  - `void deleteById(String id)` - Delete review by ID
  - `boolean existsById(String id)` - Check if review exists

---

### 2. INFRASTRUCTURE LAYER

**[JpaReviewRepository.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/infrastructure/persistence/repositories/JpaReviewRepository.java)** - UPDATED
- Added `deleteById(String id)` method (inherited from JpaRepository)
- Spring Data JPA handles the database deletion

**[ReviewRepositoryImpl.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/infrastructure/persistence/repositories/ReviewRepositoryImpl.java)** - UPDATED
- Implemented `deleteById(String id)` - delegates to JpaReviewRepository
- Implemented `existsById(String id)` - checks existence before deletion

---

### 3. APPLICATION LAYER

**[DeleteReviewCommand.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/application/commands/DeleteReviewCommand.java)** - NEW
- Input DTO for deletion operation
- Fields:
  - `reviewId` - ID of review to delete
  - `userId` - ID of user requesting deletion (for authorization)

**[DeleteReviewResult.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/application/results/DeleteReviewResult.java)** - NEW
- Output DTO for successful deletion
- Fields:
  - `reviewId` - ID of deleted review
  - `message` - "Đánh giá đã được xóa thành công" (Review deleted successfully)
- Static factory method: `DeleteReviewResult.success(reviewId)`

**[ReviewDeletedEvent.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/application/events/ReviewDeletedEvent.java)** - NEW
- Application event published when review is successfully deleted
- Contains the deleted `Review` domain model (for audit trail, logging)
- Other modules can subscribe via `@EventListener` to:
  - Update product average rating
  - Log deletion for compliance
  - Send notifications
  - Trigger cleanup tasks

**[DeleteReviewUseCase.java](src/main/java/fit/iuh/kttkpm_nhom15_be/reviews/application/usecases/DeleteReviewUseCase.java)** - NEW
- Main business orchestrator for deletion
- Marked with `@Service` and `@Transactional` for atomicity
- Execution flow:
  1. Find review by ID (throws `ReviewNotFoundException` if not found)
  2. Verify user owns the review: `review.userId == command.userId`
     - Throws `UnauthorizedReviewAccessException` if mismatch
  3. Delete review via `reviewRepository.deleteById(id)`
  4. Publish `ReviewDeletedEvent` with deleted review data
  5. Return `DeleteReviewResult.success(reviewId)`

---

### 4. PRESENTATION LAYER

**[ReviewController.java](src/main/java/fit/iuh\kttkpm_nhom15_be\reviews\presentation\controllers\ReviewController.java)** - UPDATED
- Added new endpoint: `DELETE /api/v1/reviews/{reviewId}`
- Query parameter: `userId` (required, for authorization)
- Builds `DeleteReviewCommand` from path and query params
- Invokes `deleteReviewUseCase.execute(command)`
- Returns HTTP 200 OK with `DeleteReviewResult`
- Added new exception handlers:
  - `ReviewNotFoundException` → 404 NOT_FOUND
  - `UnauthorizedReviewAccessException` → 403 FORBIDDEN

---

## REST API Endpoint

### Delete Review
```http
DELETE /api/v1/reviews/{reviewId}?userId={userId}
```

**Example Request:**
```bash
DELETE /api/v1/reviews/review-uuid-001?userId=user-uuid-123
```

**Response: 200 OK**
```json
{
  "reviewId": "review-uuid-001",
  "message": "Đánh giá đã được xóa thành công"
}
```

**Error Responses:**

| Status | Condition | Response |
|--------|-----------|----------|
| 404 NOT_FOUND | Review doesn't exist | `{"error": "Đánh giá không tồn tại: review-uuid-001"}` |
| 403 FORBIDDEN | User doesn't own review | `{"error": "Bạn không có quyền xóa đánh giá này..."}` |

---

## Execution Flow: Delete Review

```
Client
  │
  ├─ DELETE /api/v1/reviews/{reviewId}?userId={userId}
  │
  ↓
ReviewController.deleteReview(reviewId, userId)
  │
  ├─ Build DeleteReviewCommand {reviewId, userId}
  │
  ↓
DeleteReviewUseCase.execute(command)
  │
  ├─ [1] ReviewRepository.findById(reviewId)
  │       ✗ throw ReviewNotFoundException (not found)
  │       ✓ found → proceed
  │
  ├─ [2] Verify ownership: review.userId == command.userId
  │       ✗ throw UnauthorizedReviewAccessException (unauthorized)
  │       ✓ matches → proceed
  │
  ├─ [3] ReviewRepository.deleteById(reviewId)
  │       └─ ReviewRepositoryImpl.deleteById()
  │          └─ JpaReviewRepository.deleteById()
  │             └─ SQL: DELETE FROM reviews WHERE id = reviewId
  │
  ├─ [4] ApplicationEventPublisher.publishEvent(ReviewDeletedEvent)
  │       (Async listeners can subscribe, e.g., audit, logging, cleanup)
  │
  ├─ [5] Build and return DeleteReviewResult.success(reviewId)
  │       {reviewId, message: "Đánh giá đã được xóa thành công"}
  │
  ↓
ReviewController.deleteReview() returns
  HTTP 200 OK
  Body: DeleteReviewResult (JSON)
  │
  ↓
Client receives response
```

---

## Key Design Decisions

1. **User Parameter in Query String**
   - `DELETE /api/v1/reviews/{reviewId}?userId={userId}`
   - Not from JWT/SecurityContext (simpler for now)
   - Can be refactored to extract from Spring Security if Auth is added

2. **Authorization Check Before Deletion**
   - Verifies `review.userId == command.userId`
   - Prevents users from deleting others' reviews
   - Fails fast with 403 FORBIDDEN

3. **Event Publishing**
   - `ReviewDeletedEvent` published after successful deletion
   - Contains full review data (for audit/logging)
   - Allows other modules to react to deletion

4. **Idempotency**
   - Deleting non-existent review throws `ReviewNotFoundException`
   - Not idempotent by design (prevents accidental deletion via retry)
   - Use cases where multiple deletes are OK: update idempotency key check

5. **HTTP 200 OK, not 204**
   - Returns `DeleteReviewResult` with confirmation message
   - Follows same pattern as other APIs
   - Client receives confirmation of what was deleted

---

## Dependencies Injected

- `ReviewRepository` - Domain interface, impl from infrastructure layer
- `ApplicationEventPublisher` - Spring Framework built-in

---

## Error Handling Summary

| Exception | HTTP Status | Trigger |
|-----------|-------------|---------|
| `ReviewNotFoundException` | 404 | Review with given ID doesn't exist |
| `UnauthorizedReviewAccessException` | 403 | User attempting delete doesn't own review |
| `MethodArgumentTypeMismatchException` | 400 | Invalid reviewId/userId format |
| Others | 500 | Unexpected server errors |

---

## Files Created/Modified (8 Files)

### NEW Files (7)
1. `domain/exceptions/ReviewNotFoundException.java`
2. `domain/exceptions/UnauthorizedReviewAccessException.java`
3. `application/commands/DeleteReviewCommand.java`
4. `application/results/DeleteReviewResult.java`
5. `application/events/ReviewDeletedEvent.java`
6. `application/usecases/DeleteReviewUseCase.java`

### UPDATED Files (3)
1. `domain/repositories/ReviewRepository.java` - Added delete methods
2. `infrastructure/persistence/repositories/JpaReviewRepository.java` - Added deleteById
3. `infrastructure/persistence/repositories/ReviewRepositoryImpl.java` - Implemented deleteById, existsById
4. `presentation/controllers/ReviewController.java` - Added DELETE endpoint + exception handlers

---

## Testing Checklist

- [ ] Delete existing review → 200 OK
- [ ] Delete non-existent review → 404 NOT_FOUND
- [ ] Delete review owned by another user → 403 FORBIDDEN
- [ ] Verify `ReviewDeletedEvent` is published
- [ ] Verify review is actually deleted from database
- [ ] Verify deletion response contains correct reviewId and message

---

## Security Considerations

1. **Authorization**: Only review owner can delete (checked before deletion)
2. **Audit Trail**: `ReviewDeletedEvent` contains deleted review for logging
3. **Data Integrity**: Transactions ensure atomic delete
4. **User Validation**: Should ideally extract userId from JWT token (future enhancement)

---

## Future Enhancements (Out of Scope)

1. **Soft Delete**: Instead of hard delete, mark as `deleted = true`
   - Preserves audit trail
   - Allows recovery
   - Update GET endpoints to filter out soft-deleted

2. **Batch Operations**: Delete multiple reviews by admin

3. **Rate Limiting**: Prevent rapid delete/create cycles

4. **Audit Logging**: Store all deletes in audit table

5. **JWT Integration**: Extract userId from SecurityContext instead of query param

---

## Compilation Status

✅ **No errors found in reviews module**
✅ **All 4 layers properly implemented**
✅ **All dependencies resolved**
✅ **Code follows established patterns**
✅ **Exception handling complete**
✅ **Event-driven integration ready**

---

## Summary of Changes

| Layer | Change | Files |
|-------|--------|-------|
| Domain Exceptions | Added 2 new exceptions | ReviewNotFoundException, UnauthorizedReviewAccessException |
| Domain Repository | Added 2 delete operations | ReviewRepository interface |
| Infrastructure | Implemented delete operations | JpaReviewRepository, ReviewRepositoryImpl |
| Application Commands | Added delete command | DeleteReviewCommand |
| Application Results | Added delete result | DeleteReviewResult |
| Application Events | Added delete event | ReviewDeletedEvent |
| Application UseCases | Added delete orchestrator | DeleteReviewUseCase |
| Presentation | Added REST endpoint + handlers | ReviewController (DELETE endpoint + 2 exception handlers) |

