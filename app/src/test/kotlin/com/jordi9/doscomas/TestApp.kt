package com.jordi9.doscomas

import com.jordi9.doscomas.feature.greeting.inbound.GreetingConfig
import com.jordi9.doscomas.shared.outbound.metrics.MeterRegistryProvider
import com.jordi9.krat.jdbi.DatabaseConfig
import com.jordi9.krat.jdbi.JdbiProvider
import com.jordi9.krat.time.FixedTime
import com.jordi9.krat.time.TimeClock
import io.kotest.core.listeners.ProjectListener
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.mergeWith
import io.ktor.server.testing.TestApplication
import java.time.Instant
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

val SkeletonTestApp = createTestApp(stubs = Stubs)

/**
 * Creates a test application that runs the exact same [server] code path as production —
 * no test-only modules, no conditional logic, no "if (test)" branches.
 *
 * Inspired by Ratpack's MainClassUnderTest: the application code is identical,
 * only the wiring differs. We bypass the production [Registry] factory and use
 * the primary constructor directly to inject stubs and in-memory providers.
 *
 * This works because application-test.conf sets `modules = []`, preventing Ktor
 * from auto-loading the module with production dependencies.
 */
fun createTestApp(stubs: TestStubs): TestApplication = TestApplication {
  environment {
    config = defaultConfig()
  }
  application {
    val databaseConfig = DatabaseConfig(url = InMemoryDB.dbUrl, user = "test")

    server(
      greeting = GreetingConfig(message = "Test"),
      database = databaseConfig,
      tracing = stubs.openTelemetry.config,
      registry =
      Registry(
        notificationClient = stubs.notification,
        jdbiProvider =
        JdbiProvider(
          config = databaseConfig,
          openTelemetry = stubs.openTelemetry.provider.get()
        ),
        timeClock = stubs.clock,
        publicIdGenerator = stubs.publicIdGenerator,
        meterRegistryProvider = MeterRegistryProvider(),
        openTelemetryProvider = stubs.openTelemetry.provider
      )
    )
  }
}

fun httpClient(): HttpClient = SkeletonTestApp.createClient {
  install(ClientContentNegotiation) {
    json()
  }
  install(Logging) {
    logger = Logger.DEFAULT
    level = LogLevel.ALL
  }
}

fun sharedClock(): TimeClock = SharedClock.value

object SkeletonTestAppExtension : ProjectListener {
  override suspend fun beforeProject() {
    SkeletonTestApp.start()
  }

  override suspend fun afterProject() {
    SkeletonTestApp.stop()
  }
}

private object SharedClock {
  val value = FixedTime(Instant.parse("2006-01-02T15:04:05Z"))
}

private fun defaultConfig() = ApplicationConfig("application.conf")
  .mergeWith(ApplicationConfig(("application-test.conf")))
