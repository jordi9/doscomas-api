package com.jordi9.doscomas.feature.item.inbound

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.item.application.ListItemsUseCase
import com.jordi9.krat.pack.core.Handler
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

class ListItemsHandler(
  private val listItems: ListItemsUseCase
) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    val items = listItems()
    call.respond(items.map { it.toResponse() })
  }
}

fun ListItemsHandler(registry: Registry) = ListItemsHandler(
  listItems = ListItemsUseCase(registry)
)
