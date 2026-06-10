package com.jordi9.doscomas.feature.planning.inbound.space

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.application.CreateSpaceUseCase
import com.jordi9.doscomas.feature.planning.domain.SpaceName
import com.jordi9.krat.pack.core.Handler
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

class CreateSpaceHandler(
  private val createSpace: CreateSpaceUseCase
) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    val request = call.receive<Request>()
    call.respond(HttpStatusCode.Created, createSpace(SpaceName(request.name.trim())).toResponse())
  }

  @Serializable
  data class Request(val name: String)
}

fun CreateSpaceHandler(registry: Registry) = CreateSpaceHandler(
  createSpace = CreateSpaceUseCase(registry)
)
