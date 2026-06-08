package com.jordi9.doscomas.feature.planning.inbound.account

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.application.ListAccountsUseCase
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.krat.pack.core.Handler
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.util.getValue

class ListAccountsHandler(
  private val listAccounts: ListAccountsUseCase
) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    val spaceId: String by call.parameters
    call.respond(listAccounts(SpaceId(spaceId)).map { it.toResponse() })
  }
}

fun ListAccountsHandler(registry: Registry) = ListAccountsHandler(
  listAccounts = ListAccountsUseCase(registry)
)
