package com.jordi9.doscomas.feature.planning.inbound.account

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.application.CreateAccountRequest
import com.jordi9.doscomas.feature.planning.application.CreateAccountUseCase
import com.jordi9.doscomas.feature.planning.domain.AccountName
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.feature.planning.inbound.stringValue
import com.jordi9.krat.pack.core.Handler
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.util.getValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

class CreateAccountHandler(
  private val createAccount: CreateAccountUseCase
) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    val spaceId: String by call.parameters
    val request = call.receive<Request>()
    val account = createAccount(
      CreateAccountRequest(
        spaceId = SpaceId(spaceId),
        name = AccountName(request.name.trim()),
        category = toAccountCategory(request.category),
        balance = toBalance(request.balance.stringValue("balance")),
        monthlyContribution = toMonthlyContribution(request.monthlyContribution.stringValue("monthlyContribution")),
        currency = toCurrency(request.currency),
        note = request.note,
        display = request.display.toAccountDisplay()
      )
    )
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
    val display: JsonElement? = null
  )
}

fun CreateAccountHandler(registry: Registry) = CreateAccountHandler(
  createAccount = CreateAccountUseCase(registry)
)
