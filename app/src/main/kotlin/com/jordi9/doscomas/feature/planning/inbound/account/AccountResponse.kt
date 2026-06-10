package com.jordi9.doscomas.feature.planning.inbound.account

import com.jordi9.doscomas.feature.planning.domain.Account
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

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
  val display: JsonObject
)

fun Account.toResponse() = AccountResponse(
  id = id.value,
  spaceId = spaceId.value,
  name = name,
  category = category.toResponse(),
  balance = balance.toResponse(),
  monthlyContribution = monthlyContribution.toResponse(),
  currency = currency,
  note = note,
  createdAt = createdAt.toString(),
  updatedAt = updatedAt.toString(),
  display = display.value
)
