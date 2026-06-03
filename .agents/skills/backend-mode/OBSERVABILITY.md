# Observability Patterns

Implement health checks, metrics, distributed tracing, and logging for Kotlin backends.

## Philosophy

1. **Simplicity over features** - Build what's needed
2. **Control over abstraction** - Own core infrastructure
3. **Pattern consistency** - Align with hexagonal architecture
4. **Minimal dependencies** - Small, focused implementations

## Health Checks

### Ratpack-Style Pattern

```kotlin
fun interface HealthCheck {
  suspend fun check(): HealthCheckResult

  val name: String
    get() = this::class.simpleName ?: "unknown"
}

data class HealthCheckResult(
  val healthy: Boolean,
  val message: String,
  val cause: Throwable? = null
) {
  companion object {
    fun healthy(message: String = "OK") = HealthCheckResult(true, message)
    fun unhealthy(message: String, cause: Throwable? = null) =
      HealthCheckResult(false, message, cause)
  }
}
```

### Implementation Example

```kotlin
class DatabaseHealthCheck(
  private val jdbi: Jdbi,
  private val timeoutSeconds: Int = 5
) : HealthCheck {
  override val name = "database"

  override suspend fun check(): HealthCheckResult = withTimeout(timeoutSeconds.seconds) {
    jdbi.withHandle<Int, Exception> { it.createQuery("SELECT 1").mapTo<Int>().one() }
    HealthCheckResult.healthy("Database connection OK")
  }
}

// Factory function
fun DatabaseHealthCheck(registry: Registry) = DatabaseHealthCheck(jdbi = registry.jdbi)
```

### Endpoints

```
GET /health             # All checks
GET /health/readiness   # Readiness probe (critical checks)
GET /health/liveness    # Simple liveness (always healthy)
GET /health/{name}      # Individual check
```

### Route Registration

```kotlin
routing {
  healthChecks(
    basePath = "/health",
    checks = listOf(
      DatabaseHealthCheck(registry),
      QueueHealthCheck(registry)
    ),
    readiness = listOf(
      DatabaseHealthCheck(registry)  // Only critical for readiness
    )
  )
}
```

## Metrics

### Micrometer with Prometheus

```kotlin
// Counter
private val processedCounter = meterRegistry.counter("orders.processed.total")
processedCounter.increment()

// Timer
private val processTimer = meterRegistry.timer("orders.duration.seconds")
processTimer.recordSuspended {
  processOrder(order)
}

// Gauge (for values that can decrease)
private val queueDepth = AtomicLong(0)
Gauge.builder("queue.depth", queueDepth) { it.get().toDouble() }
  .register(meterRegistry)
```

### Automatic HTTP Metrics (Ktor)

Ktor's `MicrometerMetrics` plugin automatically tracks:

```
http_server_requests_seconds{method="GET",route="/orders",status="200"}
http_server_requests_active
```

### Custom Application Metrics

```kotlin
class OrderMetrics(private val registry: MeterRegistry) {
  private val statusGauges = mutableMapOf<OrderStatus, AtomicLong>()

  fun recordStatusChange(from: OrderStatus, to: OrderStatus) {
    getGauge(from).decrementAndGet()
    getGauge(to).incrementAndGet()
  }

  private fun getGauge(status: OrderStatus) = statusGauges.getOrPut(status) {
    AtomicLong(0).also { gauge ->
      Gauge.builder("orders.by_status", gauge) { it.get().toDouble() }
        .tag("status", status.name)
        .register(registry)
    }
  }
}
```

## Distributed Tracing

### OpenTelemetry Setup

```hocon
# application.conf
tracing {
  serviceName = "my-service"
  otlpEndpoint = "http://localhost:4317"
  enabled = true
}
```

### What Gets Traced

**Automatic (Ktor plugin):**
- All HTTP requests with route, method, status
- Request/response timing

**Database (JDBI plugin):**
- SQL queries with `sql`, `binding`, `rows` attributes

**Custom spans:**
```kotlin
tracer.withSpan("process-order") {
  setAttribute("app.orderId", orderId.value)
  // Processing logic
}
```

### Adding Business Attributes

```kotlin
val span = Span.current()
span.setAttribute("app.orderId", orderId.value)
span.setAttribute("app.operation", "process_payment")
span.setAttribute("app.amount", amount.cents)
```

**Attribute naming:** Use `app.*` prefix for business attributes.

### Provider Pattern

```kotlin
class OpenTelemetryProvider(private val config: TracingConfig) : AutoCloseable {
  private val sdk: OpenTelemetrySdk by lazy {
    if (!config.enabled) {
      return@lazy OpenTelemetrySdk.builder().build()  // No-op
    }

    SdkTracerProvider.builder()
      .addSpanProcessor(LoggingSpanProcessor())
      .addSpanProcessor(BatchSpanProcessor.builder(otlpExporter).build())
      .build()
      .let { OpenTelemetrySdk.builder().setTracerProvider(it).build() }
  }

  fun get(): OpenTelemetry = sdk

  override fun close() {
    sdk.close()
  }
}
```

### Disabling for Local Dev

Create `application-local.conf`:
```hocon
tracing {
  enabled = false
}
```

## Request Logging

### Trace-Based Logging

Use spans for request logging via `LoggingSpanProcessor`:

```
=== TRACE [GET /api/v1/orders/{id}] (200) 45ms | abc123 ===
  app.orderId=order-1
  ↳ jdbi.Query 12ms sql=SELECT * FROM orders WHERE id = ?
  ↳ payment.charge 28ms amount=1000
===
```

### Log Levels by Status

| Status | Level |
|--------|-------|
| 2xx/3xx | INFO |
| 4xx | WARN |
| 5xx | ERROR |

### Trace ID as Correlation ID

Trace IDs correlate logs across services:

```kotlin
val traceId = Span.current().spanContext.traceId
logger.info { "Processing order, traceId=$traceId" }
```

## Architecture Placement

### Health Checks

```
shared/inbound/health/
  └── DatabaseHealthCheck.kt       # Shared infrastructure

feature/orders/inbound/health/
  └── OrderQueueHealthCheck.kt     # Feature-specific
```

### Metrics

```
feature/orders/outbound/metrics/
  └── OrderMetrics.kt              # Feature-specific metrics

shared/outbound/metrics/
  └── MeterRegistryProvider.kt     # Shared provider
```

### Tracing

```
shared/outbound/tracing/
  ├── OpenTelemetryConfig.kt
  └── OpenTelemetryProvider.kt
```

## Monitoring Checklist

### Key Integration Points to Monitor

1. **External API calls**
   - Success/failure rate
   - Duration
   - Error messages

2. **Background jobs**
   - Queue depth
   - Processing rate
   - Failure count

3. **Database operations**
   - Query duration
   - Connection pool usage

### Alerting Triggers

- **Repeated failures**: 3+ consecutive failures
- **Pattern changes**: Sudden drop in success rate
- **Performance degradation**: 3x slower than baseline
- **Schema issues**: Spike in parsing errors

