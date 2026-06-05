# Hexagonal Architecture

Detailed patterns for layer placement and architecture rules.

## Package Structure

```
feature/[name]/
├── application/   # Use cases (orchestration)
├── domain/        # Business logic, entities, ports
├── inbound/       # REST handlers, DTOs
└── outbound/      # Repositories, external clients

shared/
├── inbound/       # Cross-cutting: health, metrics, CORS
└── outbound/      # Shared infra: database, metrics providers

CRITICAL: Outbound mirrors domain sub-domains!
domain/payment/Gateway.kt → outbound/payment/StripeGateway.kt
NOT outbound/stripe/ (wrong - tech-focused)
```

## Layer Responsibilities

### Domain (`domain/`)
- **Contains**: Entities, Value Objects, Domain Services, Ports, Exceptions
- **Rules**: NO framework dependencies, Pure Kotlin only
- **No adapter representations**: No `apiValue`, `fromApi`, `toResponse`, database column values, or JSON strings in domain types
- **Methods**: `suspend fun execute()` for domain services
- **Exception**: Can depend on metrics classes (infrastructure, not business logic)

### Application (`application/`)
- **Contains**: Use Cases (orchestration only)
- **Rules**: MANDATORY - handlers MUST call use cases, no business logic here
- **Methods**: `suspend operator fun invoke()`

### Inbound (`inbound/`)
- **Contains**: Handlers, Request/Response DTOs, Mappers
- **Rules**: DTOs use `@Serializable`, nested inside handler class
- **Mappers**: Convert HTTP/JSON contract values to/from domain (`"private_pension"` → `AccountCategory.PRIVATE_PENSION`)
- **Mapper shape**: Prefer private/local extensions on narrow types (`AccountCategory.toResponse()`) or top-level functions (`toAccountCategory(value)`); avoid extending broad primitives like `String.toAccountCategory()`
- **Shared DTOs**: Multiple handlers use same DTO? Standalone file is OK
- **Parameters**: Use Ktor delegation pattern with `import io.ktor.server.util.getValue`

### Outbound (`outbound/`)
- **Contains**: Repositories, Clients, Metrics implementations
- **Rules**: Mirrors domain structure, feature-specific (not centralized)
- **Mappers**: Convert persistence/external-service representations to/from domain; do not reuse inbound mappers from outbound

### Configuration Placement

Configuration co-locates with the layer that owns it:

| Layer       | Config Type          | Example                           |
|-------------|----------------------|-----------------------------------|
| Inbound     | API/handler config   | `GreetingConfig`, `ApiConfig`     |
| Application | Use case config      | `DownloadConfig`                  |
| Outbound    | Adapter/infra config | `DatabaseConfig`, `TracingConfig` |

## Critical Rules

### 1. Domain Purity - NO Framework Dependencies

```kotlin
// DOMAIN: Pure Kotlin, no framework annotations
data class Order(val id: OrderId, val total: Money)

// INBOUND: DTOs can have framework annotations
data class OrderResponse(val id: Long, val total: BigDecimal)

// HANDLER: Map domain → DTO
fun Order.toResponse() = OrderResponse(id.value, total.amount)
```

Adapter values also stay out of domain:

```kotlin
// WRONG: API contract leaked into domain
enum class AccountCategory(val apiValue: String) {
  CASH("cash");

  companion object {
    fun fromApi(value: String): AccountCategory = TODO()
  }
}

// CORRECT: Domain is just domain language
enum class AccountCategory {
  CASH
}

// INBOUND: API contract mapping lives beside handlers/responses
internal fun toAccountCategory(value: String): AccountCategory = when (value) {
  "cash" -> AccountCategory.CASH
  else -> throw IllegalArgumentException("Invalid account category")
}

private fun AccountCategory.toResponse(): String = when (this) {
  AccountCategory.CASH -> "cash"
}
```

See [KTOR.md](KTOR.md) for serialization patterns.

### 2. Handler → Use Case → Domain (Mandatory)

```kotlin
// CORRECT: Handler calls use case
class CreateOrderHandler(private val useCase: CreateOrderUseCase) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    val result = useCase(request.toDomain())
    call.respond(result.toResponse())
  }
}

// WRONG: Handler calls repository directly
class CreateOrderHandler(private val repository: OrderRepository) // Skip use case!
```

### 3. Handler Implementation

Handlers extract parameters, call use cases, and return responses.
See [KTOR.md](KTOR.md) for framework-specific handler patterns.

### 4. Ports (Interfaces) Belong in Domain

```kotlin
// CORRECT: Interface in domain
feature/payment/domain/PaymentGateway.kt      // Interface
feature/payment/outbound/StripeGateway.kt     // Implementation

// WRONG: Interface in outbound
feature/payment/outbound/PaymentGateway.kt    // Don't do this!
```

### 5. Outbound Mirrors Domain Structure

```kotlin
// CORRECT: Domain-focused
domain/download/AudioDownloader.kt
outbound/download/YtDlpDownloader.kt  // Mirrors domain!

// WRONG: Tech-focused
outbound/ytdlp/YtDlpDownloader.kt     // Based on technology, not domain
```

### 6. Value Objects for Type Safety

```kotlin
// CORRECT: Value objects prevent parameter mix-ups
interface FileProcessor {
  fun process(input: InputPath, output: OutputPath): Result
}

// WRONG: Primitive strings can be swapped
interface FileProcessor {
  fun process(input: String, output: String): Result  // Which is which?
}
```

#### When to Create Value Objects

| Scenario | Create Value Object? |
|----------|---------------------|
| Entity ID (OrderId, UserId) | Yes — prevents mixing IDs |
| File path with semantic meaning | Yes — `AudioPath` vs `ThumbnailPath` |
| Constrained string (email, URL) | Yes — validation at construction |
| Plain data field (name, count) | No — overhead without benefit |

#### Value Object Placement

```
feature/orders/domain/
  ├── OrderId.kt           # Simple ID wrapper
  ├── Money.kt             # Multi-field value object
  └── order/Order.kt       # Entity using value objects
```

See [KOTLIN.md](KOTLIN.md) for `@JvmInline value class` and `data class` patterns.

### 7. Error Handling Patterns

```kotlin
// Operation-specific errors: Sealed class in domain
sealed class ImportError(message: String) : Exception(message) {
  data class NoDescription(val id: VideoId) : ImportError("No description for $id")
  data class NoTracksFound(val id: VideoId) : ImportError("No tracks found in $id")
}

// Shared/reusable errors: Regular exception in domain
class VideoNotFoundException(message: String) : Exception(message)
```

**When to use which:**
- **Sealed class**: Operation returns multiple distinct failure modes, caller handles each differently
- **Regular exception**: Common error reused across operations (not found, validation failed)

## Use Case vs Domain Service

### Use Cases (Application Layer)
- **Purpose**: Orchestration (workflow sequencing)
- **Method**: `suspend operator fun invoke()`
- **Contains**: "If download succeeds, then extract metadata" (workflow)

### Domain Services (Domain Layer)
- **Purpose**: Business logic and rules
- **Method**: `suspend fun execute()`
- **Contains**: "Only ERROR videos can retry" (domain invariant)

### Quick Test: Is This Orchestration or Business Rule?

| Question    | Use Case (OK)             | Domain Service (Extract!)         |
|-------------|---------------------------|-----------------------------------|
| What is it? | Workflow decision         | Domain invariant                  |
| Example     | "On success, notify user" | "Discount is 10% if total > $100" |
| Reusable?   | Specific to workflow      | Should be enforced everywhere     |

## Detection Patterns

### Domain Purity
```bash
# Find framework imports in domain
grep -r "import io\.\|import org\.jdbi\|@Serializable" */domain/

# Find adapter representation leaks in domain
grep -r "apiValue\|fromApi\|toApi\|fromDb\|toDb\|toResponse" */domain/
```

### Fat Use Cases
```bash
# Business logic in application layer
grep -r "if.*\.status\|when.*\.status" */application/
grep -r "metrics\." */application/  # Metrics should pair with domain logic
```

### Wrong Structure
```bash
# Tech-focused outbound (should be domain-focused)
ls */outbound/  # Look for technology names (stripe/, kafka/) vs domain names (payment/, events/)
```

## Quick Decision Tree

**New Handler?**
→ `feature/[name]/inbound/[subdomain]/`
→ MUST call use case in `application/`

**New Repository/Client?**
→ `feature/[name]/outbound/[subdomain]/`
→ Mirrors domain structure

**New Business Logic?**
→ Complex rules/validation? → Domain service in `domain/`
→ Pure orchestration? → Keep in use case

**Need Interface (Port)?**
→ Multiple implementations? → Yes, create in `domain/`
→ Just one? → No, use concrete class

**Cross-Feature Communication?**
→ Through application layer or events
→ Never direct domain-to-domain
