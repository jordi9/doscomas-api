package com.jordi9.doscomas.shared.outbound.metrics

import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

class MeterRegistryProvider : AutoCloseable {
  private val registry: PrometheusMeterRegistry by lazy {
    PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
  }

  val meterBinders: List<MeterBinder> =
    listOf(
      ClassLoaderMetrics(),
      JvmMemoryMetrics(),
      JvmGcMetrics(),
      JvmThreadMetrics(),
      ProcessorMetrics(),
      FileDescriptorMetrics(),
      UptimeMetrics(),
      LogbackMetrics()
    )

  fun get(): PrometheusMeterRegistry = registry

  override fun close() {
    registry.close()
  }
}
