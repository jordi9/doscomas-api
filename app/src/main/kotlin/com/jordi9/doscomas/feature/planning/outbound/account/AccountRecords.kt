package com.jordi9.doscomas.feature.planning.outbound.account

import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import kotlinx.serialization.json.Json

data class AccountRecord(
  val id: String,
  val spaceId: String,
  val name: String,
  val category: String,
  val balanceCents: Long,
  val monthlyContributionCents: Long,
  val currency: String,
  val note: String?,
  val displayJson: String,
  val createdAt: Long,
  val updatedAt: Long
)

fun Account.toRecord() = AccountRecord(
  id = id.value,
  spaceId = spaceId.value,
  name = name.value,
  category = AccountCategoryMapper.toDatabase(category),
  balanceCents = balance.cents,
  monthlyContributionCents = monthlyContribution.cents,
  currency = CurrencyMapper.toDatabase(currency),
  note = note,
  displayJson = display.toJsonText(),
  createdAt = createdAt.toEpochMilli(),
  updatedAt = updatedAt.toEpochMilli()
)

fun AccountDisplay.toJsonText(): String = Json.encodeToString(value)
