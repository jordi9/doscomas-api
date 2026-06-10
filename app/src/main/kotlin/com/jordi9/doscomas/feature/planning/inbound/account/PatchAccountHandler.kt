package com.jordi9.doscomas.feature.planning.inbound.account

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.application.AccountBalanceUpdate
import com.jordi9.doscomas.feature.planning.application.AccountCategoryUpdate
import com.jordi9.doscomas.feature.planning.application.AccountCurrencyUpdate
import com.jordi9.doscomas.feature.planning.application.AccountDisplayUpdate
import com.jordi9.doscomas.feature.planning.application.AccountMonthlyContributionUpdate
import com.jordi9.doscomas.feature.planning.application.AccountNameUpdate
import com.jordi9.doscomas.feature.planning.application.AccountNoteUpdate
import com.jordi9.doscomas.feature.planning.application.AccountUpdate
import com.jordi9.doscomas.feature.planning.application.UpdateAccountRequest
import com.jordi9.doscomas.feature.planning.application.UpdateAccountUseCase
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.AccountName
import com.jordi9.doscomas.feature.planning.inbound.rejectUnknownFields
import com.jordi9.doscomas.feature.planning.inbound.stringValue
import com.jordi9.doscomas.feature.planning.inbound.validateRequest
import com.jordi9.krat.pack.core.Handler
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.util.getValue
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

class PatchAccountHandler(
  private val updateAccount: UpdateAccountUseCase
) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    val accountId: String by call.parameters
    val body = call.receive<JsonObject>()
    val account = updateAccount(body.toUpdateRequest(AccountId(accountId)))
    call.respond(account.toResponse())
  }
}

private fun JsonObject.toUpdateRequest(accountId: AccountId): UpdateAccountRequest {
  rejectUnknownFields(EDITABLE_FIELDS, "account")

  return UpdateAccountRequest(
    accountId = accountId,
    updates = listOfNotNull(
      stringUpdate("name") { AccountNameUpdate(AccountName(it.trim())) },
      stringUpdate("category") { AccountCategoryUpdate(toAccountCategory(it)) },
      stringUpdate("balance") { AccountBalanceUpdate(toBalance(it)) },
      stringUpdate("monthlyContribution") { AccountMonthlyContributionUpdate(toMonthlyContribution(it)) },
      stringUpdate("currency") { AccountCurrencyUpdate(toCurrency(it)) },
      nullableStringUpdate("note", ::AccountNoteUpdate),
      displayUpdate()
    )
  )
}

private fun JsonObject.stringUpdate(field: String, build: (String) -> AccountUpdate): AccountUpdate? {
  val element = this[field] ?: return null
  validateRequest(element !is JsonNull) { "$field cannot be null" }
  return build(element.stringValue(field))
}

private fun JsonObject.nullableStringUpdate(field: String, build: (String?) -> AccountUpdate): AccountUpdate? {
  val element = this[field] ?: return null
  if (element is JsonNull) return build(null)
  return build(element.stringValue(field))
}

private fun JsonObject.displayUpdate(): AccountUpdate? {
  val element = this["display"] ?: return null
  return AccountDisplayUpdate(element.toAccountDisplay())
}

private val EDITABLE_FIELDS = setOf("name", "category", "balance", "monthlyContribution", "currency", "note", "display")

fun PatchAccountHandler(registry: Registry) = PatchAccountHandler(
  updateAccount = UpdateAccountUseCase(registry)
)
