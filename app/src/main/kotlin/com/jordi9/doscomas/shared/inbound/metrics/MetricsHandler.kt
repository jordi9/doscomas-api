package com.jordi9.doscomas.shared.inbound.metrics

import com.jordi9.doscomas.Registry
import com.jordi9.krat.pack.core.Handler
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondText
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

class MetricsHandler(
  private val registry: PrometheusMeterRegistry
) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    call.respondText(
      text = registry.scrape(),
      contentType = ContentType.parse("text/plain; version=0.0.4; charset=utf-8")
    )
  }
}

fun MetricsHandler(registry: Registry) = MetricsHandler(
  registry = registry.meterRegistry
)
