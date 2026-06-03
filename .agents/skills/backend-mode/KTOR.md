# Ktor Framework Patterns

Ktor-specific patterns for HTTP handlers, routing, and serialization.

## Handler Structure

Handlers live in `inbound/` and implement a consistent pattern:

```kotlin
class CreateOrderHandler(private val useCase: CreateOrderUseCase) : Handler {
  @Serializable data class Request(val items: List<String>)
  @Serializable data class Response(val id: Long)

  override suspend fun handle(call: ApplicationCall) {
    val request = call.receive<Request>()
    val orderId = useCase(request.toDomain())
    call.respond(HttpStatusCode.Created, Response(orderId.value))
  }
}

// Factory function
fun CreateOrderHandler(registry: Registry) = CreateOrderHandler(
  useCase = CreateOrderUseCase(registry)
)
```

**Key points:**
- DTOs nested inside handler (unless shared across handlers)
- Factory function wires dependencies
- Handler calls use case, never repository directly

## Parameter Extraction

Use Ktor's delegation pattern for path parameters:

```kotlin
import io.ktor.server.util.getValue

class GetOrderHandler(private val useCase: GetOrderUseCase) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    val id: Long by call.parameters  // Throws if missing or invalid
    val order = useCase(OrderId(id))
    call.respond(order.toResponse())
  }
}
```

**Why delegation?**
- Type conversion built-in (`Long`, `Int`, `String`)
- Consistent error handling via `StatusPages`
- Cleaner than manual `?.toLongOrNull()` checks

**Avoid:**
```kotlin
// WRONG: Manual extraction with null checks
val id = call.parameters["id"]?.toLongOrNull()
  ?: return call.respond(HttpStatusCode.BadRequest, "Invalid ID")
```

## StatusPages Exception Handling

StatusPages provides centralized exception handling. This is **required** when using parameter delegation - the delegation throws exceptions that StatusPages converts to HTTP responses.

```kotlin
install(StatusPages) {
  // Parameter extraction errors (from delegation pattern)
  exception<MissingRequestParameterException> { call, e ->
    call.respond(HttpStatusCode.BadRequest, "Missing parameter: ${e.parameterName}")
  }
  exception<ParameterConversionException> { call, e ->
    call.respond(HttpStatusCode.BadRequest, "Invalid ${e.parameterName}: ${e.cause?.message}")
  }

  // Domain exceptions (from use cases)
  exception<NotFoundException> { call, e ->
    call.respond(HttpStatusCode.NotFound, e.message ?: "Not found")
  }
  exception<ValidationException> { call, e ->
    call.respond(HttpStatusCode.BadRequest, e.message ?: "Validation failed")
  }

  // Catch-all for unexpected errors
  exception<Throwable> { call, e ->
    call.respond(HttpStatusCode.InternalServerError, "Internal error")
    logger.error(e) { "Unhandled exception" }
  }
}
```

**Key principle:** Handlers don't catch exceptions. They throw domain exceptions, and StatusPages maps them to HTTP responses. This keeps handlers clean and error handling consistent.

## Route Setup

Register handlers in routing configuration:

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

**Alternative with route grouping:**
```kotlin
routing {
  route("/orders") {
    post { CreateOrderHandler(registry).handle(call) }
    get("/{id}") { GetOrderHandler(registry).handle(call) }
    post("/{id}/process") { ProcessOrderHandler(registry).handle(call) }
  }
}
```

## Serialization with kotlinx.serialization

Use `@Serializable` for DTOs in `inbound/` layer only:

```kotlin
// CORRECT: DTOs in inbound have @Serializable
@Serializable
data class OrderResponse(val id: Long, val total: BigDecimal)

// WRONG: Domain objects should NOT have @Serializable
@Serializable  // Don't do this!
data class Order(val id: OrderId, val total: Money)
```

**Mapping between layers:**
```kotlin
// Domain to DTO
fun Order.toResponse() = OrderResponse(id.value, total.amount)

// Request to Domain
fun CreateOrderRequest.toDomain() = CreateOrder(items = items.map(::OrderItem))
```

## Application Configuration

Server startup with dependency wiring:

```kotlin
fun main() {
  val config = loadConfig()
  val registry = createRegistry(config)

  embeddedServer(Netty, port = config.server.port) {
    install(ContentNegotiation) { json() }
    install(StatusPages) { /* ... */ }
    configureRouting(registry)
  }.start(wait = true)
}
```

## Ktor Plugins

### Metrics (Micrometer)

```kotlin
install(MicrometerMetrics) {
  registry = meterRegistry
  // Automatic HTTP metrics: http_server_requests_seconds
}
```

### Request Logging

```kotlin
install(CallLogging) {
  level = Level.INFO
  filter { call -> call.request.path().startsWith("/api") }
}
```

### CORS

```kotlin
install(CORS) {
  allowHost("localhost:3000")
  allowHeader(HttpHeaders.ContentType)
  allowMethod(HttpMethod.Put)
  allowMethod(HttpMethod.Delete)
}
```

## Testing Handlers

For handler tests, use Ktor's test application:

```kotlin
class OrderHandlerTest : StringSpec({
  "should create order" {
    testApplication {
      application { configureRouting(testRegistry) }

      val response = client.post("/orders") {
        contentType(ContentType.Application.Json)
        setBody("""{"items": ["item1"]}""")
      }

      response.status shouldBe HttpStatusCode.Created
    }
  }
})
```

See [TESTING.md](TESTING.md) for testing patterns.
