package com.jordi9.doscomas.feature.planning.inbound

import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountCategory
import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import kotlinx.serialization.Serializable

@Serializable
data class AccountResponse(
  val id: String,
  val spaceId: String,
  val name: String,
  val category: String,
  val balance: String,
  val monthlyContribution: String,
  val currency: String,
  val note: String?,
  val createdAt: String,
  val updatedAt: String,
  val display: Map<String, String>
)

fun Account.toResponse() = AccountResponse(
  id = id.value,
  spaceId = spaceId.value,
  name = name,
  category = category.toResponse(),
  balance = balance.toApiString(),
  monthlyContribution = monthlyContribution.toApiString(),
  currency = currency,
  note = note,
  createdAt = createdAt.toString(),
  updatedAt = updatedAt.toString(),
  display = display.toResponse()
)

private fun AccountCategory.toResponse(): String = when (this) {
  AccountCategory.CASH -> "cash"
  AccountCategory.INVESTMENT -> "investment"
  AccountCategory.PRIVATE_PENSION -> "private_pension"
  AccountCategory.REAL_ESTATE -> "real_estate"
  AccountCategory.SOCIAL_SECURITY -> "social_security"
  AccountCategory.OTHER -> "other"
}

private fun AccountDisplay.toResponse(): Map<String, String> = buildMap {
  initials?.let { put("initials", it) }
  color?.let { put("color", it) }
  typeLabel?.let { put("typeLabel", it) }
  subtitle?.let { put("subtitle", it) }
}
