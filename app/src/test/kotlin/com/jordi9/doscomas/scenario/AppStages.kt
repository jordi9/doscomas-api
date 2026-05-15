package com.jordi9.doscomas.scenario

import com.jordi9.doscomas.SkeletonTestApp
import com.jordi9.doscomas.httpClient
import com.jordi9.doscomas.jdbi
import com.jordi9.kogiven.StageContext
import com.jordi9.kogiven.required
import com.jordi9.krat.jdbi.handleSync
import com.jordi9.krat.pack.test.OpenApiSpec
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import org.jdbi.v3.core.kotlin.mapTo

class AppContext {
  var migrationCount: Int = 0
  var response: HttpResponse by required()
  var openApiSpec: OpenApiSpec by required()
}

class GivenApp : StageContext<GivenApp, AppContext>() {
  fun `the application has started`() = apply {
    // Migrations run automatically on startup
  }
}

class WhenApp : StageContext<WhenApp, AppContext>() {
  fun `querying the database schema`() = apply {
    ctx.migrationCount = jdbi().handleSync {
      createQuery("SELECT COUNT(*) FROM flyway_schema_history")
        .mapTo<Int>()
        .one()
    }
  }

  suspend fun `checking the liveness health endpoint`() = apply {
    ctx.response = httpClient().get("/health/liveness")
  }

  suspend fun `checking the readiness health endpoint`() = apply {
    ctx.response = httpClient().get("/health/readiness")
  }

  suspend fun `requesting the metrics endpoint`() = apply {
    ctx.response = httpClient().get("/metrics")
  }

  suspend fun `making an HTTP request to hello endpoint`() = apply {
    httpClient().get("/hello")
  }

  suspend fun `sending a CORS preflight request from localhost 5173`() = apply {
    ctx.response = httpClient().options("/api/v1/items") {
      header(HttpHeaders.Origin, "http://localhost:5173")
      header(HttpHeaders.AccessControlRequestMethod, "GET")
      header(HttpHeaders.AccessControlRequestHeaders, "Content-Type")
    }
  }

  suspend fun `sending an actual request with Origin header from localhost 5173`() = apply {
    ctx.response = httpClient().get("/api/v1/items") {
      header(HttpHeaders.Origin, "http://localhost:5173")
    }
  }

  fun `loading the OpenAPI spec`() = apply {
    ctx.openApiSpec = OpenApiSpec(SkeletonTestApp.application)
  }
}

class ThenApp : StageContext<ThenApp, AppContext>() {
  fun `migrations have run`() = apply {
    ctx.migrationCount shouldBeGreaterThan 0
  }

  suspend fun `the health check is healthy`() = apply {
    ctx.response.status shouldBe HttpStatusCode.OK
    ctx.response.bodyAsText() shouldBe "liveness: HEALTHY"
  }

  suspend fun `the readiness check is healthy`() = apply {
    ctx.response.status shouldBe HttpStatusCode.OK
    ctx.response.bodyAsText() shouldContain "database"
    ctx.response.bodyAsText() shouldContain "HEALTHY"
  }

  suspend fun `metrics are returned in Prometheus format`() = apply {
    ctx.response.status shouldBe HttpStatusCode.OK
    ctx.response.headers[HttpHeaders.ContentType] shouldStartWith "text/plain"
    ctx.response.bodyAsText() shouldContain "jvm_"
  }

  suspend fun `metrics contain request counter for hello endpoint`() = apply {
    ctx.response.bodyAsText() shouldContain "http_server_requests_seconds_count"
    ctx.response.bodyAsText() shouldContain "method=\"GET\""
    ctx.response.bodyAsText() shouldContain "route=\"/hello\""
    ctx.response.bodyAsText() shouldContain "status=\"200\""
  }

  suspend fun `metrics contain request duration histogram`() = apply {
    ctx.response.bodyAsText() shouldContain "http_server_requests_seconds{"
    ctx.response.bodyAsText() shouldContain "http_server_requests_seconds_sum"
    ctx.response.bodyAsText() shouldContain "http_server_requests_seconds_max"
  }

  fun `CORS preflight response allows the origin`() = apply {
    ctx.response.status shouldBe HttpStatusCode.OK
    ctx.response.headers[HttpHeaders.AccessControlAllowOrigin] shouldBe "http://localhost:5173"
    ctx.response.headers[HttpHeaders.AccessControlAllowHeaders] shouldContain "Content-Type"
  }

  fun `CORS actual response includes allow origin header`() = apply {
    ctx.response.status shouldBe HttpStatusCode.OK
    ctx.response.headers[HttpHeaders.AccessControlAllowOrigin] shouldBe "http://localhost:5173"
  }

  fun `every registered route is documented in openapi yaml`() = apply {
    val undocumented = ctx.openApiSpec.registeredFilteredRoutes - ctx.openApiSpec.documentedRoutes
    withClue("Routes registered in code but missing from openapi.yaml: $undocumented") {
      undocumented.shouldBeEmpty()
    }
  }

  fun `no orphaned routes exist in openapi yaml`() = apply {
    val orphaned = ctx.openApiSpec.documentedRoutes - ctx.openApiSpec.registeredRoutes
    withClue("Routes documented in openapi.yaml but not registered in code: $orphaned") {
      orphaned.shouldBeEmpty()
    }
  }
}
