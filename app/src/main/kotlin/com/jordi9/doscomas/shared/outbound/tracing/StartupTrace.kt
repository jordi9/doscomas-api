package com.jordi9.doscomas.shared.outbound.tracing

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span

inline fun <T> OpenTelemetry.withStartupTrace(serviceName: String, block: Span.() -> T): T {
  val span =
    getTracer(serviceName)
      .spanBuilder("ktor-start")
      .startSpan()
  return try {
    span.makeCurrent().use {
      span.block()
    }
  } finally {
    span.end()
  }
}
