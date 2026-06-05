package com.jordi9.doscomas.feature.planning.outbound

import com.jordi9.doscomas.feature.planning.domain.AccountBalanceUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountCategoryUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountCoreUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountCurrencyUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.AccountMonthlyContributionUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountNameUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountNoteUpdate
import java.time.Instant

internal data class AccountUpdateStatement(
  val sql: String,
  val params: Any
)

internal data class TextUpdateRecord(
  val id: String,
  val value: String,
  val now: Long
)

internal data class NullableTextUpdateRecord(
  val id: String,
  val value: String?,
  val now: Long
)

internal data class LongUpdateRecord(
  val id: String,
  val value: Long,
  val now: Long
)

internal data class TouchAccountRecord(
  val id: String,
  val updatedAt: Long
)

internal fun AccountCoreUpdate.toStatement(accountId: AccountId, now: Instant): AccountUpdateStatement = when (this) {
  is AccountNameUpdate -> AccountUpdateStatement(
    UPDATE_NAME,
    TextUpdateRecord(accountId.value, value, now.toEpochMilli())
  )

  is AccountCategoryUpdate -> AccountUpdateStatement(
    UPDATE_CATEGORY,
    TextUpdateRecord(accountId.value, AccountCategoryMapper.toDatabase(value), now.toEpochMilli())
  )

  is AccountBalanceUpdate -> AccountUpdateStatement(
    UPDATE_BALANCE,
    LongUpdateRecord(accountId.value, value.cents, now.toEpochMilli())
  )

  is AccountMonthlyContributionUpdate -> AccountUpdateStatement(
    UPDATE_MONTHLY_CONTRIBUTION,
    LongUpdateRecord(accountId.value, value.cents, now.toEpochMilli())
  )

  is AccountCurrencyUpdate -> AccountUpdateStatement(
    UPDATE_CURRENCY,
    TextUpdateRecord(accountId.value, value, now.toEpochMilli())
  )

  is AccountNoteUpdate -> AccountUpdateStatement(
    UPDATE_NOTE,
    NullableTextUpdateRecord(accountId.value, value, now.toEpochMilli())
  )
}

internal const val INSERT_ACCOUNT = """
  INSERT INTO accounts (
    id,
    space_id,
    name,
    category,
    balance_cents,
    monthly_contribution_cents,
    currency,
    note,
    balance_updated_at,
    created_at,
    updated_at
  ) VALUES (
    :id,
    :spaceId,
    :name,
    :category,
    :balanceCents,
    :monthlyContributionCents,
    :currency,
    :note,
    :balanceUpdatedAt,
    :createdAt,
    :updatedAt
  )
"""

private const val UPDATE_NAME = """
  UPDATE accounts
  SET name = :value,
      updated_at = :now
  WHERE id = :id
"""

private const val UPDATE_CATEGORY = """
  UPDATE accounts
  SET category = :value,
      updated_at = :now
  WHERE id = :id
"""

private const val UPDATE_BALANCE = """
  UPDATE accounts
  SET balance_cents = :value,
      balance_updated_at = :now,
      updated_at = :now
  WHERE id = :id
"""

private const val UPDATE_MONTHLY_CONTRIBUTION = """
  UPDATE accounts
  SET monthly_contribution_cents = :value,
      updated_at = :now
  WHERE id = :id
"""

private const val UPDATE_CURRENCY = """
  UPDATE accounts
  SET currency = :value,
      updated_at = :now
  WHERE id = :id
"""

private const val UPDATE_NOTE = """
  UPDATE accounts
  SET note = :value,
      updated_at = :now
  WHERE id = :id
"""

internal const val TOUCH_ACCOUNT = """
  UPDATE accounts
  SET updated_at = :updatedAt
  WHERE id = :id
"""

internal const val DELETE_DISPLAY = """
  DELETE FROM account_displays
  WHERE account_id = :accountId
"""

internal const val UPSERT_DISPLAY = """
  INSERT INTO account_displays (account_id, initials, color, type_label, subtitle)
  VALUES (:accountId, :initials, :color, :typeLabel, :subtitle)
  ON CONFLICT(account_id) DO UPDATE SET
    initials = excluded.initials,
    color = excluded.color,
    type_label = excluded.type_label,
    subtitle = excluded.subtitle
"""
