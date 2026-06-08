package com.jordi9.doscomas.feature.planning.inbound.account

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.application.GetAccountUseCase
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.krat.pack.core.Handler
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.util.getValue

class GetAccountHandler(
  private val getAccount: GetAccountUseCase
) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    val accountId: String by call.parameters
    call.respond(getAccount(AccountId(accountId)).toResponse())
  }
}

fun GetAccountHandler(registry: Registry) = GetAccountHandler(
  getAccount = GetAccountUseCase(registry)
)
