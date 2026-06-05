# Kotlin Language Patterns

Idiomatic Kotlin patterns used throughout the codebase.

## Method References

When a lambda just passes its argument to a function, use a method reference:

```kotlin
// GOOD: Method reference
val jdbi: Jdbi by lazy { jdbiProvider.get().also(::registerMappers) }

// AVOID: Unnecessary lambda
val jdbi: Jdbi by lazy { jdbiProvider.get().also { registerMappers(it) } }
```

**When to use:**
- Single-argument lambdas that just call a function
- `also`, `let`, `apply` blocks with simple delegation

## Value Classes

Use `@JvmInline value class` for type-safe wrappers with zero runtime overhead:

```kotlin
// Simple wrapper (no validation): Zero overhead at runtime
@JvmInline value class OrderId(val value: Long)
@JvmInline value class AudioPath(val value: Path)

// Usage
fun findOrder(id: OrderId): Order?  // Can't accidentally pass UserId
```

**When to use:**
- Entity IDs (`OrderId`, `UserId`) - prevents mixing IDs
- Typed paths (`AudioPath`, `ThumbnailPath`) - semantic meaning
- Single-value wrappers without validation

**When NOT to use:**
- Need validation at construction → use `data class` with `init`
- Multiple fields → use regular `data class`

## Data Classes with Validation

Use `data class` with `init` block when you need construction-time validation:

```kotlin
data class Email(val value: String) {
  init {
    require(value.contains("@")) { "Invalid email: $value" }
  }
}

data class Money(val amount: BigDecimal, val currency: Currency) {
  init {
    require(amount >= BigDecimal.ZERO) { "Amount cannot be negative" }
  }
}
```

## Lazy Delegation

Use `by lazy` for deferred initialization:

```kotlin
class Registry(private val jdbiProvider: JdbiProvider) {
  // Initialized on first access, cached afterward
  val jdbi: Jdbi by lazy { jdbiProvider.get().also(::registerMappers) }
}
```

**Rules:**
- Provider already lazy? Use `get()` not `by lazy` — see [DI.md](DI.md) "Don't Double-Lazy"
- Need side effect on first access? Use `by lazy { provider.get().also(::fn) }`

## Sealed Classes for Errors

Use sealed classes when an operation has multiple distinct failure modes:

```kotlin
sealed class ImportError(message: String) : Exception(message) {
  data class NoDescription(val id: VideoId) : ImportError("No description for $id")
  data class NoTracksFound(val id: VideoId) : ImportError("No tracks found in $id")
}

// Usage: Exhaustive when
when (error) {
  is ImportError.NoDescription -> handleNoDescription(error.id)
  is ImportError.NoTracksFound -> handleNoTracks(error.id)
}
```

**When to use:**
- Caller handles each failure mode differently
- Operation has 2+ distinct error types
- Want compile-time exhaustiveness checking

**When NOT to use:**
- Common/reusable errors → regular exception class
- Single failure mode → throw standard exception

## Operator Invoke

Use `operator fun invoke()` for callable classes:

```kotlin
class CreateOrderUseCase(private val repository: OrderRepository) {
  suspend operator fun invoke(request: CreateOrderRequest): OrderId {
    // Use case logic
  }
}

// Usage: Call like a function
val orderId = createOrderUseCase(request)
```

**Convention:** Use for use cases in the application layer.

## Suspend Functions

Mark functions `suspend` when they perform async operations:

```kotlin
// Use case (application layer)
suspend operator fun invoke(id: OrderId): Order

// Domain service
suspend fun execute(id: OrderId): ProcessResult

// Repository
suspend fun findById(id: OrderId): Order?
```

**Guidelines:**
- IO operations (database, network) should be `suspend`
- Pure computation doesn't need `suspend`
- Propagate `suspend` up the call chain

## Extension Functions

Use extension functions for transformations and conversions on narrow project types:

```kotlin
// Domain to DTO mapping
fun Order.toResponse() = OrderResponse(id.value, total.amount)

// Request to domain mapping
fun CreateOrderRequest.toDomain() = CreateOrder(items = items.map(::OrderItem))
```

Avoid extensions on broad primitives for semantic parsing:

```kotlin
// GOOD: Explicit mapper function
internal fun toAccountCategory(value: String): AccountCategory = TODO()

// AVOID: Makes every String look category-aware
internal fun String.toAccountCategory(): AccountCategory = TODO()
```

**Placement:**
- Mapper extensions: Same file as the target type or where used if file-private
- Primitive/contract mappers: Top-level functions in adapter `Mappers.kt`
- Utility extensions: Dedicated `Extensions.kt` in the package
