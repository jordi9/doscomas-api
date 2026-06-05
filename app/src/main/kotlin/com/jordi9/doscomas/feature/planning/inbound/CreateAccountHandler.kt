package com.jordi9.doscomas.feature.planning.inbound

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.application.CreateAccountCommand
import com.jordi9.doscomas.feature.planning.application.CreateAccountUseCase
import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import com.jordi9.doscomas.feature.planning.domain.Money
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.krat.pack.core.Handler
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.util.getValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

class CreateAccountHandler(
  private val createAccount: CreateAccountUseCase
) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    val spaceId: String by call.parameters
    val request = call.receive<Request>()
    val account = createAccount(request.toCommand(SpaceId(spaceId)))
    call.respond(HttpStatusCode.Created, account.toResponse())
  }

  @Serializable
  data class Request(
    val name: String,
    val category: String,
    val balance: JsonPrimitive,
    val monthlyContribution: JsonPrimitive = JsonPrimitive("0.00"),
    val currency: String = "EUR",
    val note: String? = null,
    val display: AccountDisplayRequest? = null
  )
}

private fun CreateAccountHandler.Request.toCommand(spaceId: SpaceId) = CreateAccountCommand(
  spaceId = spaceId,
  name = name,
  category = toAccountCategory(category),
  balance = Money.parse(balance.stringValue("balance")),
  monthlyContribution = Money.parse(monthlyContribution.stringValue("monthlyContribution")),
  currency = currency,
  note = note,
  display = display?.toDomain() ?: AccountDisplay()
)

fun CreateAccountHandler(registry: Registry) = CreateAccountHandler(
  createAccount = CreateAccountUseCase(registry)
)
