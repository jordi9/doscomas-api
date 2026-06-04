package com.jordi9.doscomas

import com.jordi9.doscomas.shared.domain.PublicIdGenerator
import com.jordi9.doscomas.stub.NotificationClientStub
import com.jordi9.krat.otel.OpenTelemetryConfig
import com.jordi9.krat.otel.testlib.OpenTelemetryTestProvider
import com.jordi9.krat.time.TimeClock
import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult

class TestStubs(
  val clock: TimeClock = sharedClock(),
  val publicIdGenerator: PublicIdGenerator = PublicIdGenerator(seed = 42),
  val notification: NotificationClientStub = NotificationClientStub(),
  val openTelemetry: OpenTelemetryTestProvider =
    OpenTelemetryTestProvider(
      OpenTelemetryConfig(serviceName = "doscomas-test")
    )
) {
  fun resetAll() {
    notification.reset()
    publicIdGenerator.reset()
  }
}

val Stubs = TestStubs()
val NotificationStub = Stubs.notification

object ResetStubsExtension : AfterEachListener {
  override suspend fun afterEach(testCase: TestCase, result: TestResult) {
    Stubs.resetAll()
  }
}
