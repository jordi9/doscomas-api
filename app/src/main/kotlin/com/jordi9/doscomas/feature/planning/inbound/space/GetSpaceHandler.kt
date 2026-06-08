package com.jordi9.doscomas.feature.planning.inbound.space

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.application.GetSpaceUseCase
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.krat.pack.core.Handler
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.util.getValue

class GetSpaceHandler(
  private val getSpace: GetSpaceUseCase
) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    val spaceId: String by call.parameters
    call.respond(getSpace(SpaceId(spaceId)).toResponse())
  }
}

fun GetSpaceHandler(registry: Registry) = GetSpaceHandler(
  getSpace = GetSpaceUseCase(registry)
)
