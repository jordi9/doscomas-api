package com.jordi9.doscomas.feature.planning.inbound

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.application.PatchAccountUseCase
import com.jordi9.doscomas.feature.planning.domain.AccountChanges
import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.Money
import com.jordi9.doscomas.feature.planning.domain.map
import com.jordi9.krat.pack.core.Handler
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.util.getValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

class PatchAccountHandler(
  private val patchAccount: PatchAccountUseCase
) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    val accountId: String by call.parameters
    val body = call.receive<JsonObject>()
    val account = patchAccount(AccountId(accountId), body.toAccountChanges())
    call.respond(account.toResponse())
  }
}

private fun JsonObject.toAccountChanges(): AccountChanges {
  rejectUnknownFields(EDITABLE_FIELDS, "account")

  return AccountChanges(
    name = stringUpdate("name"),
    category = stringUpdate("category", ::toAccountCategory),
    balance = stringUpdate("balance", Money::parse),
    monthlyContribution = stringUpdate("monthlyContribution", Money::parse),
    currency = stringUpdate("currency"),
    note = nullableStringUpdate("note"),
    display = nullableUpdate("display") { it.displayValue() }.map { it ?: AccountDisplay() }
  )
}

private fun JsonElement.displayValue(): AccountDisplay {
  val display = this as? JsonObject ?: badRequest("display must be an object")
  return Json.decodeFromJsonElement<AccountDisplayRequest>(display).toDomain()
}

private val EDITABLE_FIELDS = setOf("name", "category", "balance", "monthlyContribution", "currency", "note", "display")

fun PatchAccountHandler(registry: Registry) = PatchAccountHandler(
  patchAccount = PatchAccountUseCase(registry)
)
