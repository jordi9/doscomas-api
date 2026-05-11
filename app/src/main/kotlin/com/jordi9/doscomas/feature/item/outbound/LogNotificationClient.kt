package com.jordi9.doscomas.feature.item.outbound

import com.jordi9.doscomas.feature.item.domain.NotificationClient
import com.jordi9.krat.otel.withSpan
import io.opentelemetry.api.OpenTelemetry

class LogNotificationClient(
  openTelemetry: OpenTelemetry
) : NotificationClient {
  private val tracer = openTelemetry.getTracer("doscomas.notifications")

  override fun notify(message: String) {
    tracer.withSpan("notification.send", { setAttribute("notification.message", message) }) {}
  }
}
