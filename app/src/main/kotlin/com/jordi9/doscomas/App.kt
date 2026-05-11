package com.jordi9.doscomas

import com.jordi9.doscomas.feature.greeting.inbound.GreetingConfig
import com.jordi9.doscomas.feature.greeting.inbound.HelloHandler
import com.jordi9.doscomas.feature.item.inbound.CreateItemHandler
import com.jordi9.doscomas.feature.item.inbound.GetItemHandler
import com.jordi9.doscomas.feature.item.inbound.ListItemsHandler
import com.jordi9.doscomas.shared.inbound.handler.installErrorHandling
import com.jordi9.doscomas.shared.inbound.health.DatabaseHealthCheck
import com.jordi9.doscomas.shared.inbound.metrics.MetricsHandler
import com.jordi9.doscomas.shared.outbound.db.runDatabaseMigrations
import com.jordi9.doscomas.shared.outbound.tracing.withStartupTrace
import com.jordi9.krat.jdbi.DatabaseConfig
import com.jordi9.krat.otel.OpenTelemetryConfig
import com.jordi9.krat.pack.core.config
import com.jordi9.krat.pack.core.get
import com.jordi9.krat.pack.core.healthChecks
import com.jordi9.krat.pack.core.post
import com.jordi9.krat.pack.cors.CorsConfig
import com.jordi9.krat.pack.cors.installCors
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import io.opentelemetry.instrumentation.ktor.v3_0.KtorServerTelemetry

fun main(args: Array<String>) {
  EngineMain.main(args)
}

fun Application.server(
  greeting: GreetingConfig = config("greeting"),
  database: DatabaseConfig = config("database"),
  tracing: OpenTelemetryConfig = config("tracing"),
  cors: CorsConfig = config("cors"),
  registry: Registry = Registry(database, tracing)
) {
  registry.openTelemetry.withStartupTrace("doscomas") {
    runDatabaseMigrations(database.url)

    installContentNegotiation()
    installCors(cors)
    installOpenTelemetry(registry)
    installErrorHandling()
    installMicrometer(registry)
    installShutdownHook(registry)
  }

  routes(greeting, registry)
}

fun Application.routes(greeting: GreetingConfig, registry: Registry) {
  routing {
    get("/hello", HelloHandler(greeting))
    get("/", HelloHandler(greeting))

    get("/api/v1/items", ListItemsHandler(registry))
    get("/api/v1/items/{id}", GetItemHandler(registry))
    post("/api/v1/items", CreateItemHandler(registry))

    get("/metrics", MetricsHandler(registry))
    installHealthChecks(registry)
  }
}

private fun Routing.installHealthChecks(registry: Registry) {
  healthChecks(
    basePath = "/health",
    checks = listOf(DatabaseHealthCheck(registry)),
    readiness = listOf(DatabaseHealthCheck(registry))
  )
}

private fun Application.installContentNegotiation() {
  install(ContentNegotiation) {
    json()
  }
}

private fun Application.installOpenTelemetry(registry: Registry) {
  if (otelIsNotInstalled()) {
    System.setProperty("doscomas.otel.installed", "true")
    install(KtorServerTelemetry) {
      setOpenTelemetry(registry.openTelemetry)
    }
  }
}

// Only install once - KtorServerTelemetry doesn't survive Ktor auto-reload well
// Use system property to survive classloader reloading
private fun otelIsNotInstalled(): Boolean = System.getProperty("doscomas.otel.installed") != "true"

private fun Application.installMicrometer(registry: Registry) {
  install(MicrometerMetrics) {
    this.registry = registry.meterRegistry
    metricName = "http.server.requests"
    meterBinders = registry.meterBinders
  }
}

private fun Application.installShutdownHook(registry: Registry) {
  monitor.subscribe(ApplicationStopped) {
    registry.close()
  }
}
