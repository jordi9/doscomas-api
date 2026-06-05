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
    val balance: String,
    val monthlyContribution: String = "0.00",
    val currency: String = "EUR",
    val note: String? = null,
    val display: DisplayRequest? = null
  ) {
    @Serializable
    data class DisplayRequest(
      val initials: String? = null,
      val color: String? = null,
      val typeLabel: String? = null,
      val subtitle: String? = null
    )
  }
}

private fun CreateAccountHandler.Request.toCommand(spaceId: SpaceId) = CreateAccountCommand(
  spaceId = spaceId,
  name = name,
  category = toAccountCategory(category),
  balance = Money.parse(balance),
  monthlyContribution = Money.parse(monthlyContribution),
  currency = currency,
  note = note,
  display = display?.toDomain() ?: AccountDisplay()
)

private fun CreateAccountHandler.Request.DisplayRequest.toDomain() = AccountDisplay(
  initials = initials,
  color = color,
  typeLabel = typeLabel,
  subtitle = subtitle
)

fun CreateAccountHandler(registry: Registry) = CreateAccountHandler(
  createAccount = CreateAccountUseCase(registry)
)
