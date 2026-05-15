package com.jordi9.doscomas.shared.outbound.tracing

import com.jordi9.krat.otel.recordError
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer

inline fun Application.withStartupTracer(openTelemetry: OpenTelemetry, serviceName: String, block: Tracer.() -> Unit) {
  val tracer = openTelemetry.getTracer(serviceName)

  val span = tracer
    .spanBuilder("ktor-start")
    .setSpanKind(SpanKind.INTERNAL)
    .startSpan()

  try {
    span.makeCurrent().use {
      block(tracer)
    }
  } catch (e: Throwable) {
    span.recordError(e)
    span.end()
    throw e
  }

  monitor.subscribe(ApplicationStarted) { span.end() }
}
