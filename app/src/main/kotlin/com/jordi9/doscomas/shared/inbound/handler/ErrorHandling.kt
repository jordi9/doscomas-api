package com.jordi9.doscomas.shared.inbound.handler

import com.jordi9.doscomas.feature.item.domain.ItemNotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import kotlinx.serialization.Serializable

fun Application.installErrorHandling() {
  install(StatusPages) {
    exception<ItemNotFoundException> { call, e -> call.clientError(HttpStatusCode.NotFound, e) }
    exception<IllegalArgumentException> { call, e -> call.clientError(HttpStatusCode.BadRequest, e) }
    exception<MissingRequestParameterException> { call, e ->
      call.clientError(HttpStatusCode.BadRequest, e, "Missing parameter: ${e.parameterName}")
    }
    exception<NumberFormatException> { call, e ->
      call.clientError(HttpStatusCode.BadRequest, e, "Invalid ID format")
    }

    exception<Throwable> { call, e -> call.serverError(HttpStatusCode.InternalServerError, e) }
  }
}

private suspend fun ApplicationCall.clientError(status: HttpStatusCode, e: Throwable, message: String? = e.message) {
  respond(status, ErrorResponse(message ?: "Unknown exception"))
}

private suspend fun ApplicationCall.serverError(status: HttpStatusCode, e: Throwable) {
  Span.current().setStatus(StatusCode.ERROR, e.message ?: "Unknown error")
  respond(status, ErrorResponse(e.message ?: "Error"))
}

@Serializable
private data class ErrorResponse(
  val error: String
)
