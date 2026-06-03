# Testing Strategy

Test Kotlin backends using the honeycomb model with BDD-style component tests.

## Philosophy: Test in Production

- **Component tests** validate business behavior (primary focus)
- **Unit tests** for complex isolated logic (secondary)
- **No integration tests** against external APIs (flaky, slow, false confidence)
- **Strong monitoring** catches real-world failures in production

## Testing Shape: Honeycomb

```
         ┌─────────────────┐
         │   E2E (few)     │  ← Avoid for single services
         ├─────────────────┤
    ████████████████████████  ← Component tests (primary)
         ├─────────────────┤
         │  Unit (sparse)  │  ← Only for complex domain logic
         └─────────────────┘
```

**Why honeycomb?** Modern services have more logic in integration with collaborators (databases, other services) than in individual objects.

## Canon TDD Workflow

1. **Write a list** of test scenarios you want to cover
2. Turn **exactly one** item into a failing test
3. **Change the code** just enough to pass (and all previous tests)
4. Optionally **refactor** keeping tests green
5. **Repeat** until list is empty

## BDD with Kogiven

Use Given/When/Then structure for readable, behavior-focused tests.

### File Organization

```
app/src/test/kotlin/scenario/
├── OrderStages.kt    # Context + Given/When/Then stages
└── OrderShould.kt    # Test scenarios only
```

**Principle:** Scenarios stay clean and readable, implementation details live in stage classes.

### Context Class

Holds shared test state:

```kotlin
class OrderContext(val registry: Registry) {
  val repository = OrderRepository(registry)

  // Direct database access for test setup/verification
  fun jdbi(): Jdbi = registry.jdbi()
}
```

### Stage Classes

```kotlin
class OrderGiven(context: OrderContext) : Stage<OrderGiven, OrderContext>(context) {
  fun `order exists`(orderId: OrderId, status: OrderStatus): OrderGiven {
    context.repository.save(Order(orderId, status))
    return self
  }

  // Direct SQL for edge cases
  fun `order in database with invalid state`(orderId: OrderId): OrderGiven {
    context.jdbi().useHandle<Exception> { handle ->
      handle.execute("INSERT INTO orders (id, status) VALUES (?, ?)",
        orderId.value, "INVALID")
    }
    return self
  }
}

class OrderWhen(context: OrderContext) : Stage<OrderWhen, OrderContext>(context) {
  var result: ProcessResult? = null
  var error: Throwable? = null

  fun `processing order`(orderId: OrderId): OrderWhen {
    try {
      result = ProcessOrderUseCase(context.registry)(orderId)
    } catch (e: Exception) {
      error = e
    }
    return self
  }
}

class OrderThen(context: OrderContext) : Stage<OrderThen, OrderContext>(context) {
  fun `order status is`(orderId: OrderId, expected: OrderStatus): OrderThen {
    val order = context.repository.findById(orderId)
    order?.status shouldBe expected
    return self
  }

  fun `error is`(expected: KClass<out Exception>): OrderThen {
    (context as OrderWhen).error shouldNotBe null
    (context as OrderWhen).error!!::class shouldBe expected
    return self
  }
}
```

**Key patterns:**
- Backtick method names for natural language
- Methods return `self` for fluent chaining
- Mutable state in `When` stage stores results for assertions

### Test Scenarios

```kotlin
class OrderShould : ScenarioStringSpec<OrderGiven, OrderWhen, OrderThen, OrderContext>({
  "process order when status is PENDING" {
    val orderId = OrderId(1)

    Given.`order exists`(orderId, OrderStatus.PENDING)
    When.`processing order`(orderId)
    Then.`order status is`(orderId, OrderStatus.COMPLETED)
  }

  "reject order that is already processed" {
    val orderId = OrderId(2)

    Given.`order exists`(orderId, OrderStatus.COMPLETED)
    When.`processing order`(orderId)
    Then.`error is`(OrderAlreadyProcessedException::class)
  }
})
```

## Component Tests

### What to Test
- Feature acceptance criteria
- Request/response validation
- Database side effects
- Business behavior from public API

### Test Data Setup

| Scenario | Use |
|----------|-----|
| Domain object setup | Repository (same as production) |
| Edge cases, partial data | Direct SQL |
| Domain behavior assertions | Repository |
| Database-level verification | Direct SQL |

### Database Integration

```kotlin
class DatabaseOrderShould : ScenarioStringSpec<...>({
  val registry = createTestRegistry(inMemoryDatabase = true)

  beforeSpec {
    Flyway.configure()
      .dataSource(registry.jdbi.dataSource)
      .load()
      .migrate()
  }

  afterTest {
    registry.jdbi.useHandle<Exception> {
      it.execute("DELETE FROM orders")
    }
  }
})
```

## Test Fixtures (Factory Objects)

Centralize test defaults in factory objects. Tests only specify parameters that affect behavior.

**Location:** `app/src/test/kotlin/fixture/`

**Naming:** Plural entity names (`Videos`, `Orders`). The `fixture/` package makes the purpose clear.

```kotlin
// For domain objects
object Videos {
  fun one(
    id: VideoId = VideoId(1),
    title: String = "Test Video",
    status: VideoStatus = VideoStatus.NEW,
  ) = Video(id = id, title = title, status = status, ...)
}

// For database rows (when needed)
object Videos {
  fun inserted(
    videoId: String = uniqueVideoId(),
    title: String = "Test Video",
    status: VideoStatus = VideoStatus.NEW,
  ): VideoRow = VideoTable.insert(...)

  private fun uniqueVideoId() = "test-video-${System.nanoTime()}"
}
```

**Usage:** `Videos.one(status = VideoStatus.ERROR)` — only specify what matters for the test.

**Guidelines:**
- Use `one()` for domain objects (no side effects)
- Use `inserted()` for database setup (performs actual insert)
- Keep defaults minimal and realistic
- Generate unique IDs to avoid collisions
- Extract when 3+ tests repeat similar setup (Rule of Three)

## Kotest Patterns

### StringSpec

Use `StringSpec` for readable test names:

```kotlin
class OrderServiceTest : StringSpec({
  "should process order when status is PENDING" {
    val order = Order(OrderId(1), OrderStatus.PENDING)
    val result = service.process(order)
    result.status shouldBe OrderStatus.COMPLETED
  }
})
```

### Shared Setup

When 3+ tests repeat the same setup:

```kotlin
class OrderServiceTest : StringSpec({
  val config = createTestConfig()
  val registry = createTestRegistry(config)

  beforeTest { resetDatabase() }
  afterTest { cleanupTempFiles() }

  val client = autoClose(HttpClient())

  "test 1" { /* uses config, registry, client */ }
})
```

| Pattern | Solution |
|---------|----------|
| Same value in 3+ tests | Field: `val config = ...` |
| Setup before each test | `beforeTest { }` |
| Cleanup after each test | `afterTest { }` |
| Closeable resource | `autoClose()` |

### Assertions

Prefer exact over partial — exact assertions catch unexpected additions:

```kotlin
// GOOD: Fails if unexpected keys appear
response shouldBe mapOf("id" to "order-1", "status" to "completed")

// AVOID: Ignores unexpected keys
response shouldContainAll mapOf("status" to "completed")
```

Common assertions:

```kotlin
result shouldBe expected              // Equality
result.shouldBeNull()                 // Nullability
list shouldContain item               // Collections
list shouldHaveSize 3
shouldThrow<IllegalArgumentException> { riskyOperation() }  // Exceptions
```

**Partial assertions OK when:** testing external data, order doesn't matter, other tests cover full structure.

## What NOT to Build

### Integration Tests Against External APIs
Slow, flaky, false confidence. **Instead:** stubs in component tests + production monitoring.

### VCR/Recorded Response Fixtures
Fixtures become stale. **Instead:** test stubs representing contracts + production validation.

### Stub-Only Tests

```kotlin
// BAD: Validates stub, not integration
stub.process(item)
stub.calls.size shouldBe 1
```

## Integration Tests (When Valuable)

Use sparingly when command-line parameters or external tool behavior is error-prone.

**When valuable:** Complex CLI parameters, silent failures, behavior depends on parameter interaction.

**When to skip:** Slow/network-dependent tools, stable interfaces, component tests suffice.

## Avoid Test-Only APIs

Tests should use the same APIs as production code. **Exception:** test fixtures can have test-only APIs.

```kotlin
// BAD: Test-only convenience method
class DownloadQueue {
  suspend fun dequeue(): Item  // Only tests use this
}

// GOOD: Same API as production
class DownloadQueue {
  val channel: ReceiveChannel<Item>  // Tests and production use same API
}
```

## Traits of Good Tests

- **Fast**: Full suite < 10 minutes
- **Reliable**: No flakiness (fix or delete flaky tests)
- **Behavior-focused**: Coupled to behavior, not implementation
- **Real implementations**: Use real database (in-memory), real components
