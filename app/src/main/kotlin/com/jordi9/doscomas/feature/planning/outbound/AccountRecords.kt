package com.jordi9.doscomas.feature.planning.outbound

import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import com.jordi9.doscomas.feature.planning.domain.AccountId

internal data class AccountRecord(
  val id: String,
  val spaceId: String,
  val name: String,
  val category: String,
  val balanceCents: Long,
  val monthlyContributionCents: Long,
  val currency: String,
  val note: String?,
  val balanceUpdatedAt: Long,
  val createdAt: Long,
  val updatedAt: Long
)

internal data class AccountDisplayIdRecord(
  val accountId: String
)

internal data class AccountDisplayRecord(
  val accountId: String,
  val initials: String?,
  val color: String?,
  val typeLabel: String?,
  val subtitle: String?
)

internal fun Account.toRecord() = AccountRecord(
  id = id.value,
  spaceId = spaceId.value,
  name = name,
  category = AccountCategoryMapper.toDatabase(category),
  balanceCents = balance.cents,
  monthlyContributionCents = monthlyContribution.cents,
  currency = currency,
  note = note,
  balanceUpdatedAt = balanceUpdatedAt.toEpochMilli(),
  createdAt = createdAt.toEpochMilli(),
  updatedAt = updatedAt.toEpochMilli()
)

internal fun AccountDisplay.toRecord(accountId: AccountId) = AccountDisplayRecord(
  accountId = accountId.value,
  initials = initials,
  color = color,
  typeLabel = typeLabel,
  subtitle = subtitle
)
