---
name: backend-mode
description: Implement Kotlin/Ktor backend features using TDD, hexagonal architecture, and YAGNI. Use PROACTIVELY for any Kotlin backend work — creating handlers, routes, endpoints, use cases, domain services, repositories, tests, health checks, or metrics. Triggers on Ktor, Kotest, Kogiven, JDBI, Micrometer, or kotlinx.serialization.
---

# Backend Mode

Implement Kotlin/Ktor backend features following TDD, hexagonal architecture, and YAGNI principles.

## TDD Workflow

Follow Canon TDD exactly:

```
1. Write test scenarios list
2. Turn ONE into failing test
3. Make it pass MINIMALLY
4. Refactor (keep tests green)
5. Repeat until list empty
```

**Before writing tests:** Read [TESTING.md](TESTING.md) for honeycomb model and BDD patterns.
**Before layer decisions:** Read [HEXAGONAL.md](HEXAGONAL.md) for placement rules.
**Before setting up DI:** Read [DI.md](DI.md) for Registry and factory patterns.

**Commands:**
```bash
./gradlew test --tests "*Should.scenario name*"  # Run one test
./gradlew test                                    # Run all tests
./gradlew ktlintFormat                            # Format code
```

## Quick Decisions

### Where Does This Go?

| Need          | Create            | Layer                       |
|---------------|-------------------|-----------------------------|
| HTTP endpoint | Handler + UseCase | `inbound/` + `application/` |
| Business rule | Domain Service    | `domain/`                   |
| Data access   | Repository        | `outbound/`                 |
| External API  | Client            | `outbound/`                 |
| Type safety   | Value Object      | `domain/`                   |

### Layer Flow

```
Handler (inbound/) → UseCase (application/) → DomainService (domain/) → Repository (outbound/)
```

**Critical:** Handlers MUST call use cases, never repositories directly.

### Method Naming Convention

| Layer       | Method Style                    | Example                          |
|-------------|---------------------------------|----------------------------------|
| Use cases   | `suspend operator fun invoke()` | Callable like functions          |
| Domain svc  | `suspend fun execute()`         | Explicit method calls            |

### When to Use Value Objects

| Wrap This         | Example                                                         |
|-------------------|-----------------------------------------------------------------|
| Entity IDs        | `@JvmInline value class OrderId(val value: Long)`               |
| File paths        | `@JvmInline value class AudioPath(val value: Path)`             |
| Constrained types | `data class Email(val value: String) { init { require(...) } }` |

**Rule:** If two parameters could be swapped by mistake, wrap them. See [HEXAGONAL.md](HEXAGONAL.md) for patterns.

### YAGNI Checklist

Before adding code, ask:
- [ ] Was this explicitly requested?
- [ ] Am I creating an interface with one implementation? → Use concrete class
- [ ] Am I adding "just in case" error handling? → Remove it
- [ ] Am I over-abstracting? → Simplify
- [ ] Am I adding comments that repeat what the code says? → Delete them

**Comments rule:** Don't add comments to self-explanatory code. Good code is its own documentation. Only add comments when explaining *why* something non-obvious is done, never *what* the code does.

## Reference Files

**You MUST read these files during implementation:**

| Task                      | Read First                           |
|---------------------------|--------------------------------------|
| Writing tests             | [TESTING.md](TESTING.md)             |
| Layer placement decisions | [HEXAGONAL.md](HEXAGONAL.md)         |
| Setting up dependencies   | [DI.md](DI.md)                       |
| Creating handlers/routes  | [KTOR.md](KTOR.md)                   |
| Adding metrics/health     | [OBSERVABILITY.md](OBSERVABILITY.md) |
| Kotlin idioms/patterns    | [KOTLIN.md](KOTLIN.md)               |

## Examples

### Good: Handler Calling Use Case

```kotlin
// inbound/orders/CreateOrderHandler.kt
class CreateOrderHandler(private val useCase: CreateOrderUseCase) : Handler {
  @Serializable data class Request(val items: List<String>)
  @Serializable data class Response(val id: Long)

  override suspend fun handle(call: ApplicationCall) {
    val request = call.receive<Request>()
    val orderId = useCase(request.toDomain())
    call.respond(HttpStatusCode.Created, Response(orderId.value))
  }
}

fun CreateOrderHandler(registry: Registry) = CreateOrderHandler(
  useCase = CreateOrderUseCase(registry)
)
```

### Bad: Handler Calling Repository Directly

```kotlin
// WRONG - skips application layer
class CreateOrderHandler(private val repository: OrderRepository) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    val id = repository.save(order)  // Direct repository call!
  }
}
```

### Good: Domain Without Framework

```kotlin
// domain/orders/Order.kt - NO @Serializable, NO Ktor, NO JDBI
data class Order(
  val id: OrderId,
  val items: List<OrderItem>,
  val status: OrderStatus
) {
  fun canProcess(): Boolean = status == OrderStatus.PENDING && items.isNotEmpty()
}

@JvmInline value class OrderId(val value: Long)
```

### Bad: Domain With Framework

```kotlin
// WRONG - framework annotation in domain
@Serializable  // Don't do this in domain!
data class Order(val id: Long, val status: String)
```

### Good: BDD Test

```kotlin
// scenario/OrderShould.kt
class OrderShould : ScenarioStringSpec<OrderGiven, OrderWhen, OrderThen, OrderContext>({
  "process order when status is PENDING" {
    val orderId = OrderId(1)

    Given.`order exists`(orderId, OrderStatus.PENDING)
    When.`processing order`(orderId)
    Then.`order status is`(orderId, OrderStatus.COMPLETED)
  }
})
```

### Good: Domain Service with Metrics

```kotlin
// domain/orders/RetryOrder.kt - Business logic + metrics together
class RetryOrder(
  private val orderRepository: OrderRepository,
  private val orderMetrics: OrderMetrics
) {
  suspend fun execute(id: OrderId): Order {
    val change = orderRepository.retry(id)
    orderMetrics.onStatusChange(change.before.status, change.after.status)
    return change.after
  }
}

fun RetryOrder(registry: Registry) = RetryOrder(
  orderRepository = OrderRepository(registry),
  orderMetrics = registry.orderMetrics
)
```

**Why metrics in domain?** Metrics track business events, not infrastructure. They pair with the logic that triggers them.

## Finalize Checklist

Before declaring done:
- [ ] `./gradlew ktlintFormat` — Code formatted
- [ ] `./gradlew test` — All tests pass
- [ ] `./gradlew build` — Build succeeds
- [ ] Every class has factory function taking Registry
- [ ] Domain has NO framework imports
- [ ] Handlers call use cases (not repositories)
- [ ] OpenAPI updated if handlers/DTOs changed
