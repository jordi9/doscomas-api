package com.jordi9.doscomas

import com.jordi9.doscomas.stub.NotificationClientStub
import com.jordi9.krat.otel.OpenTelemetryConfig
import com.jordi9.krat.otel.testlib.OpenTelemetryTestProvider
import com.jordi9.krat.time.FixedTime
import com.jordi9.krat.time.TimeClock
import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import java.time.Instant

class TestStubs(
  val clock: TimeClock = FixedTime(Instant.parse("2006-01-02T15:04:05Z")),
  val notification: NotificationClientStub = NotificationClientStub(),
  val openTelemetry: OpenTelemetryTestProvider =
    OpenTelemetryTestProvider(
      OpenTelemetryConfig(serviceName = "doscomas-test")
    )
) {
  fun resetAll() {
    notification.reset()
  }
}

val Stubs = TestStubs()
val NotificationStub = Stubs.notification

object ResetStubsExtension : AfterEachListener {
  override suspend fun afterEach(testCase: TestCase, result: TestResult) {
    Stubs.resetAll()
  }
}
