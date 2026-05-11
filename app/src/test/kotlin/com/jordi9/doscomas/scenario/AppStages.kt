package com.jordi9.doscomas.scenario

import com.jordi9.doscomas.httpClient
import com.jordi9.doscomas.jdbi
import com.jordi9.kogiven.StageContext
import com.jordi9.kogiven.required
import com.jordi9.krat.jdbi.handleSync
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import org.jdbi.v3.core.kotlin.mapTo

class AppContext {
  var migrationCount: Int = 0
  var healthCheckStatus: HttpStatusCode by required()
  var healthCheckResponse: String by required()
  var readinessStatus: HttpStatusCode by required()
  var readinessResponse: String by required()
  var metricsStatus: HttpStatusCode by required()
  var metricsResponse: String by required()
  var metricsContentType: String by required()
  var corsPreflightStatus: HttpStatusCode by required()
  var corsAllowOrigin: String? = null
  var corsAllowMethods: String? = null
  var corsAllowHeaders: String? = null
  var corsActualRequestStatus: HttpStatusCode by required()
  var corsActualRequestAllowOrigin: String? = null
}

class GivenApp : StageContext<GivenApp, AppContext>() {
  fun `the application has started`() = apply {
    // Migrations run automatically on startup
  }
}

class WhenApp : StageContext<WhenApp, AppContext>() {
  fun `querying the database schema`() = apply {
    ctx.migrationCount =
      jdbi().handleSync {
        createQuery("SELECT COUNT(*) FROM flyway_schema_history")
          .mapTo<Int>()
          .one()
      }
  }

  suspend fun `checking the liveness health endpoint`() = apply {
    val response = httpClient().get("/health/liveness")
    ctx.healthCheckStatus = response.status
    ctx.healthCheckResponse = response.bodyAsText()
  }

  suspend fun `checking the readiness health endpoint`() = apply {
    val response = httpClient().get("/health/readiness")
    ctx.readinessStatus = response.status
    ctx.readinessResponse = response.bodyAsText()
  }

  suspend fun `requesting the metrics endpoint`() = apply {
    val response = httpClient().get("/metrics")
    ctx.metricsStatus = response.status
    ctx.metricsResponse = response.bodyAsText()
    ctx.metricsContentType = response.headers["Content-Type"] ?: ""
  }

  suspend fun `making an HTTP request to hello endpoint`() = apply {
    httpClient().get("/hello")
  }

  suspend fun `sending a CORS preflight request from localhost 5173`() = apply {
    val response =
      httpClient().options("/api/v1/items") {
        header(HttpHeaders.Origin, "http://localhost:5173")
        header(HttpHeaders.AccessControlRequestMethod, "GET")
        header(HttpHeaders.AccessControlRequestHeaders, "Content-Type")
      }
    ctx.corsPreflightStatus = response.status
    ctx.corsAllowOrigin = response.headers[HttpHeaders.AccessControlAllowOrigin]
    ctx.corsAllowMethods = response.headers[HttpHeaders.AccessControlAllowMethods]
    ctx.corsAllowHeaders = response.headers[HttpHeaders.AccessControlAllowHeaders]
  }

  suspend fun `sending an actual request with Origin header from localhost 5173`() = apply {
    val response =
      httpClient().get("/api/v1/items") {
        header(HttpHeaders.Origin, "http://localhost:5173")
      }
    ctx.corsActualRequestStatus = response.status
    ctx.corsActualRequestAllowOrigin = response.headers[HttpHeaders.AccessControlAllowOrigin]
  }
}

class ThenApp : StageContext<ThenApp, AppContext>() {
  fun `migrations have run`() = apply {
    ctx.migrationCount shouldBeGreaterThan 0
  }

  fun `the health check is healthy`() = apply {
    ctx.healthCheckStatus shouldBe HttpStatusCode.OK
    ctx.healthCheckResponse shouldBe "liveness: HEALTHY"
  }

  fun `the readiness check is healthy`() = apply {
    ctx.readinessStatus shouldBe HttpStatusCode.OK
    ctx.readinessResponse shouldContain "database"
    ctx.readinessResponse shouldContain "HEALTHY"
  }

  fun `metrics are returned in Prometheus format`() = apply {
    ctx.metricsStatus shouldBe HttpStatusCode.OK
    ctx.metricsContentType shouldStartWith "text/plain"
    ctx.metricsResponse shouldContain "jvm_"
  }

  fun `metrics contain request counter for hello endpoint`() = apply {
    ctx.metricsResponse shouldContain "http_server_requests_seconds_count"
    ctx.metricsResponse shouldContain "method=\"GET\""
    ctx.metricsResponse shouldContain "route=\"/hello\""
    ctx.metricsResponse shouldContain "status=\"200\""
  }

  fun `metrics contain request duration histogram`() = apply {
    ctx.metricsResponse shouldContain "http_server_requests_seconds{"
    ctx.metricsResponse shouldContain "http_server_requests_seconds_sum"
    ctx.metricsResponse shouldContain "http_server_requests_seconds_max"
  }

  fun `CORS preflight response allows the origin`() = apply {
    ctx.corsPreflightStatus shouldBe HttpStatusCode.OK
    ctx.corsAllowOrigin shouldBe "http://localhost:5173"
    ctx.corsAllowHeaders shouldContain "Content-Type"
  }

  fun `CORS actual response includes allow origin header`() = apply {
    ctx.corsActualRequestStatus shouldBe HttpStatusCode.OK
    ctx.corsActualRequestAllowOrigin shouldBe "http://localhost:5173"
  }
}
