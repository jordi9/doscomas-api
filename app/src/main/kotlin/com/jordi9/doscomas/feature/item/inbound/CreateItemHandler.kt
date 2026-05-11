package com.jordi9.doscomas.feature.item.inbound

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.item.application.CreateItemUseCase
import com.jordi9.krat.pack.core.Handler
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

class CreateItemHandler(
  private val createItem: CreateItemUseCase
) : Handler {
  @Serializable
  data class Request(
    val name: String,
    val description: String? = null
  )

  override suspend fun handle(call: ApplicationCall) {
    val request = call.receive<Request>()
    val item = createItem(request.name, request.description)
    call.respond(HttpStatusCode.Created, item.toResponse())
  }
}

fun CreateItemHandler(registry: Registry) = CreateItemHandler(
  createItem = CreateItemUseCase(registry)
)
