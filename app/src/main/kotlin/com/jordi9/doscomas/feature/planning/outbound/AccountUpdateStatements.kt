package com.jordi9.doscomas.feature.planning.outbound

import com.jordi9.doscomas.feature.planning.domain.AccountBalanceUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountCategoryUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountCoreUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountCurrencyUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.AccountMonthlyContributionUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountNameUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountNoteUpdate
import org.jdbi.v3.core.Handle
import java.time.Instant

fun interface AccountCoreWrite {
  fun execute(handle: Handle, accountId: AccountId, now: Instant)
}

private class AccountField<T>(column: String) {
  private val sql = accountUpdateSql(column)

  operator fun invoke(value: T): AccountCoreWrite = AccountCoreWrite { handle, accountId, now ->
    execute(handle, accountId, value, now)
  }

  private fun execute(handle: Handle, accountId: AccountId, value: T, now: Instant) {
    handle.createUpdate(sql)
      .bind("id", accountId.value)
      .bind("value", value)
      .bind("now", now.toEpochMilli())
      .execute()
  }
}

private object AccountFields {
  val name = AccountField<String>("name")
  val category = AccountField<String>("category")
  val balance = AccountField<Long>("balance_cents")
  val monthlyContribution = AccountField<Long>("monthly_contribution_cents")
  val currency = AccountField<String>("currency")
  val note = AccountField<String?>("note")
}

fun AccountCoreUpdate.toWrite(): AccountCoreWrite = when (this) {
  is AccountNameUpdate -> AccountFields.name(value)
  is AccountCategoryUpdate -> AccountFields.category(AccountCategoryMapper.toDatabase(value))
  is AccountBalanceUpdate -> AccountFields.balance(value.cents)
  is AccountMonthlyContributionUpdate -> AccountFields.monthlyContribution(value.cents)
  is AccountCurrencyUpdate -> AccountFields.currency(value)
  is AccountNoteUpdate -> AccountFields.note(value)
}

private fun accountUpdateSql(column: String): String = buildString {
  appendLine("UPDATE accounts")
  appendLine("SET $column = :value,")
  appendLine("    updated_at = :now")
  append("WHERE id = :id")
}

const val INSERT_ACCOUNT = """
  INSERT INTO accounts (
    id,
    space_id,
    name,
    category,
    balance_cents,
    monthly_contribution_cents,
    currency,
    note,
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
    :createdAt,
    :updatedAt
  )
"""

data class TouchAccountRecord(
  val id: String,
  val updatedAt: Long
)

const val TOUCH_ACCOUNT = """
  UPDATE accounts
  SET updated_at = :updatedAt
  WHERE id = :id
"""

const val DELETE_DISPLAY = """
  DELETE FROM account_displays
  WHERE account_id = :accountId
"""

const val UPSERT_DISPLAY = """
  INSERT INTO account_displays (account_id, initials, color, type_label, subtitle)
  VALUES (:accountId, :initials, :color, :typeLabel, :subtitle)
  ON CONFLICT(account_id) DO UPDATE SET
    initials = excluded.initials,
    color = excluded.color,
    type_label = excluded.type_label,
    subtitle = excluded.subtitle
"""
