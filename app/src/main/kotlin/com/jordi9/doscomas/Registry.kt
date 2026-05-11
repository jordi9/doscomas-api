package com.jordi9.doscomas

import com.jordi9.doscomas.feature.item.domain.NotificationClient
import com.jordi9.doscomas.feature.item.outbound.LogNotificationClient
import com.jordi9.doscomas.feature.item.outbound.registerItemMappers
import com.jordi9.doscomas.shared.outbound.metrics.MeterRegistryProvider
import com.jordi9.krat.jdbi.DatabaseConfig
import com.jordi9.krat.jdbi.JdbiProvider
import com.jordi9.krat.otel.OpenTelemetryConfig
import com.jordi9.krat.otel.OpenTelemetryProvider
import com.jordi9.krat.time.SystemTime
import com.jordi9.krat.time.TimeClock
import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.opentelemetry.api.OpenTelemetry
import org.jdbi.v3.core.Jdbi

class Registry(
  val notificationClient: NotificationClient,
  val timeClock: TimeClock,
  private val openTelemetryProvider: OpenTelemetryProvider,
  private val meterRegistryProvider: MeterRegistryProvider,
  private val jdbiProvider: JdbiProvider
) : AutoCloseable {
  val openTelemetry: OpenTelemetry get() = openTelemetryProvider.get()
  val meterRegistry: PrometheusMeterRegistry get() = meterRegistryProvider.get()
  val meterBinders: List<MeterBinder> get() = meterRegistryProvider.meterBinders

  val jdbi: Jdbi by lazy {
    jdbiProvider.get().also(::registerItemMappers)
  }

  override fun close() {
    jdbiProvider.close()
    openTelemetryProvider.close()
    meterRegistryProvider.close()
  }
}

fun Registry(database: DatabaseConfig, tracing: OpenTelemetryConfig): Registry {
  val openTelemetryProvider = OpenTelemetryProvider(tracing)
  val meterRegistryProvider = MeterRegistryProvider()

  return Registry(
    notificationClient = LogNotificationClient(openTelemetryProvider.get()),
    jdbiProvider =
    JdbiProvider(
      config = database,
      openTelemetry = openTelemetryProvider.get(),
      meterRegistry = meterRegistryProvider.get()
    ),
    timeClock = SystemTime,
    meterRegistryProvider = meterRegistryProvider,
    openTelemetryProvider = openTelemetryProvider
  )
}
