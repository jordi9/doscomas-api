package com.jordi9.doscomas.feature.planning.inbound.space

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.application.ListSpacesUseCase
import com.jordi9.krat.pack.core.Handler
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

class ListSpacesHandler(
  private val listSpaces: ListSpacesUseCase
) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    call.respond(listSpaces().map { it.toResponse() })
  }
}

fun ListSpacesHandler(registry: Registry) = ListSpacesHandler(
  listSpaces = ListSpacesUseCase(registry)
)
