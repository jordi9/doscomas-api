package com.jordi9.doscomas.feature.item.inbound

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.item.application.GetItemUseCase
import com.jordi9.doscomas.feature.item.domain.ItemId
import com.jordi9.krat.pack.core.Handler
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.util.getValue

class GetItemHandler(
  private val getItem: GetItemUseCase
) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    val id: Long by call.parameters
    val item = getItem(ItemId(id))
    call.respond(item.toResponse())
  }
}

fun GetItemHandler(registry: Registry) = GetItemHandler(
  getItem = GetItemUseCase(registry)
)
