package com.jordi9.doscomas.feature.planning.inbound

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.application.PatchAccountUseCase
import com.jordi9.doscomas.feature.planning.domain.AccountChanges
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.DisplayChange
import com.jordi9.doscomas.feature.planning.domain.Money
import com.jordi9.doscomas.feature.planning.domain.NullableField
import com.jordi9.krat.pack.core.Handler
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.util.getValue
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

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

private val editableFields = setOf("name", "category", "balance", "monthlyContribution", "currency", "note", "display")
private val serverOwnedFields = setOf("id", "spaceId", "createdAt", "updatedAt", "balanceUpdatedAt")
private val displayFields = setOf("initials", "color", "typeLabel", "subtitle")

private fun JsonObject.toAccountChanges(): AccountChanges {
  rejectUnknownFields(editableFields + serverOwnedFields, "account")
  rejectServerOwnedFields()

  return AccountChanges(
    name = optionalString("name"),
    category = optionalString("category")?.let(::toAccountCategory),
    balance = optionalString("balance")?.let(Money::parse),
    monthlyContribution = optionalString("monthlyContribution")?.let(Money::parse),
    currency = optionalString("currency"),
    note = nullableString("note"),
    display = displayChange()
  )
}

private fun JsonObject.rejectServerOwnedFields() {
  val found = keys.firstOrNull { it in serverOwnedFields }
  validateRequest(found == null) { "$found is server-owned" }
}

private fun JsonObject.nullableString(field: String): NullableField<String> {
  val element = this[field] ?: return NullableField.Unchanged
  if (element is JsonNull) return NullableField.Clear
  return NullableField.Set(element.stringValue(field))
}

private fun JsonObject.displayChange(): DisplayChange {
  val element = this["display"] ?: return DisplayChange.Unchanged
  if (element is JsonNull) return DisplayChange.Clear
  val display = element as? JsonObject ?: badRequest("display must be an object")
  display.rejectUnknownFields(displayFields, "display")

  return DisplayChange.Update(
    initials = display.nullableString("initials"),
    color = display.nullableString("color"),
    typeLabel = display.nullableString("typeLabel"),
    subtitle = display.nullableString("subtitle")
  )
}

fun PatchAccountHandler(registry: Registry) = PatchAccountHandler(
  patchAccount = PatchAccountUseCase(registry)
)
