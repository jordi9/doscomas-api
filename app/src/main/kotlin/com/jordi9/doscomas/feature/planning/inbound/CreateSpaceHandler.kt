package com.jordi9.doscomas.feature.planning.inbound

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.application.CreateSpaceUseCase
import com.jordi9.krat.pack.core.Handler
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

class CreateSpaceHandler(
  private val createSpace: CreateSpaceUseCase
) : Handler {
  @Serializable
  data class Request(
    val name: String
  )

  override suspend fun handle(call: ApplicationCall) {
    val request = call.receive<Request>()
    call.respond(HttpStatusCode.Created, createSpace(request.name).toResponse())
  }
}

fun CreateSpaceHandler(registry: Registry) = CreateSpaceHandler(
  createSpace = CreateSpaceUseCase(registry)
)
