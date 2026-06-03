# Manual Dependency Injection

Manual DI using Registry pattern - no frameworks, explicit wiring, full control.

## Philosophy

- **Explicit over magic** - See all wiring in code
- **No reflection** - Compile-time safety
- **Simple debugging** - Step through construction
- **No learning curve** - Just Kotlin classes

## The Registry Pattern

Registry is the central place that wires dependencies together. It's created once at application startup.

### What Goes in Registry

```kotlin
class Registry(
  private val jdbiProvider: JdbiProvider,
  private val meterRegistryProvider: MeterRegistryProvider,
  private val config: AppConfig
) {
  // Infrastructure providers (injected)
  val jdbi: Jdbi by lazy { jdbiProvider.get().also(::registerMappers) }
  val meterRegistry: MeterRegistry get() = meterRegistryProvider.get()

  // Foundational dependencies
  val timeClock: TimeClock = TimeClock.System

  // Shared infrastructure (owned by Registry)
  val downloadQueue: DownloadQueue by lazy { DownloadQueue(this) }
  val videoMetrics: VideoMetrics by lazy { VideoMetrics(this) }
}
```

### What Does NOT Go in Registry

| Don't Put       | Instead           | Why                             |
|-----------------|-------------------|---------------------------------|
| Repositories    | Factory functions | Created per-use, not singletons |
| Use cases       | Factory functions | Created per-use                 |
| Domain services | Factory functions | Created per-use                 |
| Handlers        | Factory functions | Created at route setup          |

### The Rule

**Registry holds infrastructure. Factory functions create everything else.**

```kotlin
// IN Registry: Infrastructure, providers, shared state
val jdbi: Jdbi
val meterRegistry: MeterRegistry
val downloadQueue: DownloadQueue

// NOT in Registry: Created via factory functions
fun OrderRepository(registry: Registry) = OrderRepository(registry.jdbi)
fun CreateOrderUseCase(registry: Registry) = CreateOrderUseCase(OrderRepository(registry))
fun CreateOrderHandler(registry: Registry) = CreateOrderHandler(CreateOrderUseCase(registry))
```

## Factory Functions

Every class that has dependencies should have a factory function.

### Basic Pattern

```kotlin
// The class with its real constructor
class OrderRepository(private val jdbi: Jdbi) {
  fun findById(id: OrderId): Order? { ... }
  fun save(order: Order): OrderId { ... }
}

// Factory function - same name as class
fun OrderRepository(registry: Registry) = OrderRepository(registry.jdbi)
```

### Usage

```kotlin
// At route setup
routing {
  post("/orders") { CreateOrderHandler(registry).handle(call) }
}

// In use case
class ProcessOrderUseCase(private val repository: OrderRepository) {
  suspend operator fun invoke(id: OrderId) { ... }
}

fun ProcessOrderUseCase(registry: Registry) = ProcessOrderUseCase(
  repository = OrderRepository(registry)
)
```

### Return Type Convention

**Omit return type when creating same type:**

```kotlin
// GOOD: Type is obvious
fun OrderRepository(registry: Registry) = OrderRepository(registry.jdbi)

// AVOID: Redundant
fun OrderRepository(registry: Registry): OrderRepository = OrderRepository(registry.jdbi)
```

### Exception: Signature Collision

Don't create factory with same signature as constructor:

```kotlin
// This causes compilation error!
class Service(private val client: Client, private val config: Config = Config())
fun Service(client: Client, config: Config = Config()) = Service(client, config) // ERROR

// Instead: Direct instantiation when no transformation
val service = Service(registry.client)
```

## Provider Pattern

For complex dependency creation, use a Provider class.

### When to Use Providers

- Dependency needs complex setup
- Dependency should be singleton
- Dependency needs cleanup (AutoCloseable)
- Creation logic is more than one line

### Provider Structure

```kotlin
class JdbiProvider(private val config: DatabaseConfig) : AutoCloseable {
  private val instance: Jdbi by lazy {
    Jdbi.create(config.url).apply {
      installPlugin(KotlinPlugin())
      installPlugin(SqlObjectPlugin())
    }
  }

  fun get(): Jdbi = instance

  override fun close() {
    // Cleanup if needed
  }
}
```

### Provider in Registry

```kotlin
class Registry(
  private val jdbiProvider: JdbiProvider,
  private val openTelemetryProvider: OpenTelemetryProvider
) : AutoCloseable {
  // Provider manages singleton - just delegate
  val openTelemetry: OpenTelemetry get() = openTelemetryProvider.get()

  // Side effect on first access - use lazy with also
  val jdbi: Jdbi by lazy {
    jdbiProvider.get().also(::registerMappers)
  }

  override fun close() {
    openTelemetryProvider.close()
    jdbiProvider.close()
  }
}
```

## Singleton Rules

Where the `lazy` lives determines singleton behavior.

| Scenario | Pattern | Example |
|----------|---------|---------|
| Provider manages singleton | `get() = provider.get()` | `openTelemetry`, `meterRegistry` |
| Registry owns singleton | `by lazy { ... }` | `videoMetrics`, `downloadQueue` |
| Side effect on first access | `by lazy { provider.get().also(::fn) }` | `jdbi` (registers mappers) |

### Don't Double-Lazy

```kotlin
class Registry(private val meterRegistryProvider: MeterRegistryProvider) {
  // WRONG: Double lazy
  val meterRegistry: MeterRegistry by lazy { meterRegistryProvider.get() }

  // CORRECT: Provider already has lazy, just delegate
  val meterRegistry: MeterRegistry get() = meterRegistryProvider.get()
}
```

See [KOTLIN.md](KOTLIN.md) for `lazy` delegation patterns and method references.

## Wiring at Startup

### Application Entry Point

```kotlin
fun main() {
  // 1. Load configuration
  val config = loadConfig()

  // 2. Create providers
  val jdbiProvider = JdbiProvider(config.database)
  val meterRegistryProvider = MeterRegistryProvider(config.metrics)
  val openTelemetryProvider = OpenTelemetryProvider(config.tracing)

  // 3. Create registry
  val registry = Registry(
    jdbiProvider = jdbiProvider,
    meterRegistryProvider = meterRegistryProvider,
    openTelemetryProvider = openTelemetryProvider,
    config = config
  )

  // 4. Run migrations
  Flyway.configure()
    .dataSource(config.database.url, null, null)
    .load()
    .migrate()

  // 5. Start server with registry
  embeddedServer(Netty, port = config.server.port) {
    configureRouting(registry)
  }.start(wait = true)
}
```

### Route Setup

```kotlin
fun Application.configureRouting(registry: Registry) {
  routing {
    // Each handler created via factory function
    post("/orders") { CreateOrderHandler(registry).handle(call) }
    get("/orders/{id}") { GetOrderHandler(registry).handle(call) }
    post("/orders/{id}/process") { ProcessOrderHandler(registry).handle(call) }
  }
}
```

## Anti-Patterns

### Service Locator (Don't)

```kotlin
// WRONG: Service locator
class OrderService {
  private val repository = Registry.getInstance().getOrderRepository()
}

// CORRECT: Constructor injection
class OrderService(private val repository: OrderRepository)
```

### God Registry (Don't)

```kotlin
// WRONG: Everything in Registry
class Registry {
  val orderRepository = OrderRepository(jdbi)
  val userRepository = UserRepository(jdbi)
  val productRepository = ProductRepository(jdbi)
  // ... 50 more repositories
}

// CORRECT: Only infrastructure, rest via factories
class Registry {
  val jdbi: Jdbi
  val meterRegistry: MeterRegistry
}

fun OrderRepository(registry: Registry) = OrderRepository(registry.jdbi)
```

### Creating Dependencies Inline (Don't)

```kotlin
// WRONG: Creating dependencies inline
class ProcessOrderUseCase(private val registry: Registry) {
  suspend operator fun invoke(id: OrderId) {
    val repository = OrderRepository(registry.jdbi)  // Created every call!
    // ...
  }
}

// CORRECT: Inject via constructor
class ProcessOrderUseCase(private val repository: OrderRepository) {
  suspend operator fun invoke(id: OrderId) { ... }
}

fun ProcessOrderUseCase(registry: Registry) = ProcessOrderUseCase(
  repository = OrderRepository(registry)
)
```
