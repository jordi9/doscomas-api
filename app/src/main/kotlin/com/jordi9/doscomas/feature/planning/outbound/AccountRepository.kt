package com.jordi9.doscomas.feature.planning.outbound

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountBalanceUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountCategoryUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountCurrencyUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import com.jordi9.doscomas.feature.planning.domain.AccountDisplayUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.AccountMonthlyContributionUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountNameUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountNoteUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountUpdate
import com.jordi9.doscomas.feature.planning.domain.Money
import com.jordi9.doscomas.feature.planning.domain.Space
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.krat.jdbi.handle
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.SqlStatement
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.Instant

class AccountRepository(
  private val jdbi: Jdbi
) {
  suspend fun save(account: Account): Account = jdbi.handle {
    createUpdate(
      """
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
      """.trimIndent()
    ).bindAccount(account)
      .execute()

    saveDisplay(account.id, account.display)
    createQuery(accountSelect("WHERE accounts.id = :id AND accounts.space_id = :spaceId"))
      .bind("id", account.id.value)
      .bind("spaceId", account.spaceId.value)
      .mapTo<Account>()
      .one()
  }

  suspend fun findBySpaceId(spaceId: SpaceId): List<Account> = jdbi.handle {
    createQuery(accountSelect("WHERE accounts.space_id = :spaceId ORDER BY accounts.created_at DESC"))
      .bind("spaceId", spaceId.value)
      .mapTo<Account>()
      .list()
  }

  suspend fun findById(accountId: AccountId): Account? = jdbi.handle {
    findAccount(accountId)
  }

  suspend fun update(accountId: AccountId, updates: List<AccountUpdate>, now: Instant): Account? = jdbi.handle {
    for (update in updates) {
      if (!writeUpdate(accountId, update, now)) return@handle null
    }
    findAccount(accountId)
  }

  private fun Handle.writeUpdate(accountId: AccountId, update: AccountUpdate, now: Instant): Boolean = when (update) {
    is AccountNameUpdate -> {
      updateName(accountId, update.value, now)
      true
    }

    is AccountCategoryUpdate -> {
      updateCategory(accountId, AccountCategoryMapper.toDatabase(update.value), now)
      true
    }

    is AccountBalanceUpdate -> {
      updateBalance(accountId, update.value.cents, now)
      true
    }

    is AccountMonthlyContributionUpdate -> {
      updateMonthlyContribution(accountId, update.value.cents, now)
      true
    }

    is AccountCurrencyUpdate -> {
      updateCurrency(accountId, update.value, now)
      true
    }

    is AccountNoteUpdate -> {
      updateNote(accountId, update.value, now)
      true
    }

    is AccountDisplayUpdate -> updateDisplay(accountId, update.value, now)
  }

  private fun Handle.updateName(accountId: AccountId, name: String, now: Instant) {
    createUpdate(
      """
        UPDATE accounts
        SET name = :name,
            updated_at = :now
        WHERE id = :id
      """.trimIndent()
    ).bind("id", accountId.value)
      .bind("name", name)
      .bind("now", now.toEpochMilli())
      .execute()
  }

  private fun Handle.updateCategory(accountId: AccountId, category: String, now: Instant) {
    createUpdate(
      """
        UPDATE accounts
        SET category = :category,
            updated_at = :now
        WHERE id = :id
      """.trimIndent()
    ).bind("id", accountId.value)
      .bind("category", category)
      .bind("now", now.toEpochMilli())
      .execute()
  }

  private fun Handle.updateBalance(accountId: AccountId, balanceCents: Long, now: Instant) {
    createUpdate(
      """
        UPDATE accounts
        SET balance_cents = :balanceCents,
            balance_updated_at = :now,
            updated_at = :now
        WHERE id = :id
      """.trimIndent()
    ).bind("id", accountId.value)
      .bind("balanceCents", balanceCents)
      .bind("now", now.toEpochMilli())
      .execute()
  }

  private fun Handle.updateMonthlyContribution(accountId: AccountId, monthlyContributionCents: Long, now: Instant) {
    createUpdate(
      """
        UPDATE accounts
        SET monthly_contribution_cents = :monthlyContributionCents,
            updated_at = :now
        WHERE id = :id
      """.trimIndent()
    ).bind("id", accountId.value)
      .bind("monthlyContributionCents", monthlyContributionCents)
      .bind("now", now.toEpochMilli())
      .execute()
  }

  private fun Handle.updateCurrency(accountId: AccountId, currency: String, now: Instant) {
    createUpdate(
      """
        UPDATE accounts
        SET currency = :currency,
            updated_at = :now
        WHERE id = :id
      """.trimIndent()
    ).bind("id", accountId.value)
      .bind("currency", currency)
      .bind("now", now.toEpochMilli())
      .execute()
  }

  private fun Handle.updateNote(accountId: AccountId, note: String?, now: Instant) {
    createUpdate(
      """
        UPDATE accounts
        SET note = :note,
            updated_at = :now
        WHERE id = :id
      """.trimIndent()
    ).bind("id", accountId.value)
      .bind("note", note)
      .bind("now", now.toEpochMilli())
      .execute()
  }

  private fun Handle.updateDisplay(accountId: AccountId, display: AccountDisplay, now: Instant): Boolean {
    if (touchAccount(accountId, now) == 0) return false
    saveDisplay(accountId, display)
    return true
  }

  private fun Handle.saveDisplay(accountId: AccountId, display: AccountDisplay) {
    if (display.isEmpty()) {
      createUpdate("DELETE FROM account_displays WHERE account_id = :accountId")
        .bind("accountId", accountId.value)
        .execute()
      return
    }

    createUpdate(
      """
        INSERT INTO account_displays (account_id, initials, color, type_label, subtitle)
        VALUES (:accountId, :initials, :color, :typeLabel, :subtitle)
        ON CONFLICT(account_id) DO UPDATE SET
          initials = excluded.initials,
          color = excluded.color,
          type_label = excluded.type_label,
          subtitle = excluded.subtitle
      """.trimIndent()
    ).bind("accountId", accountId.value)
      .bind("initials", display.initials)
      .bind("color", display.color)
      .bind("typeLabel", display.typeLabel)
      .bind("subtitle", display.subtitle)
      .execute()
  }

  private fun Handle.touchAccount(accountId: AccountId, now: Instant): Int = createUpdate(
    "UPDATE accounts SET updated_at = :updatedAt WHERE id = :id"
  ).bind("id", accountId.value)
    .bind("updatedAt", now.toEpochMilli())
    .execute()

  private fun Handle.findAccount(accountId: AccountId): Account? = createQuery(accountSelect("WHERE accounts.id = :id"))
    .bind("id", accountId.value)
    .mapTo<Account>()
    .findOne()
    .orElse(null)

  private fun <T : SqlStatement<T>> T.bindAccount(account: Account): T = bind("id", account.id.value)
    .bind("spaceId", account.spaceId.value)
    .bind("name", account.name)
    .bind("category", AccountCategoryMapper.toDatabase(account.category))
    .bind("balanceCents", account.balance.cents)
    .bind("monthlyContributionCents", account.monthlyContribution.cents)
    .bind("currency", account.currency)
    .bind("note", account.note)
    .bind("balanceUpdatedAt", account.balanceUpdatedAt.toEpochMilli())
    .bind("createdAt", account.createdAt.toEpochMilli())
    .bind("updatedAt", account.updatedAt.toEpochMilli())
}

class AccountRowMapper : RowMapper<Account> {
  override fun map(rs: ResultSet, ctx: StatementContext): Account {
    val display = AccountDisplay(
      initials = rs.getString("initials"),
      color = rs.getString("color"),
      typeLabel = rs.getString("type_label"),
      subtitle = rs.getString("subtitle")
    )

    return Account(
      id = AccountId(rs.getString("id")),
      spaceId = SpaceId(rs.getString("space_id")),
      name = rs.getString("name"),
      category = AccountCategoryMapper.toDomain(rs.getString("category")),
      balance = Money(rs.getLong("balance_cents")),
      monthlyContribution = Money(rs.getLong("monthly_contribution_cents")),
      currency = rs.getString("currency"),
      note = rs.getString("note"),
      balanceUpdatedAt = Instant.ofEpochMilli(rs.getLong("balance_updated_at")),
      createdAt = Instant.ofEpochMilli(rs.getLong("created_at")),
      updatedAt = Instant.ofEpochMilli(rs.getLong("updated_at")),
      display = display
    )
  }
}

fun AccountRepository(registry: Registry) = AccountRepository(
  jdbi = registry.jdbi
)

internal fun registerPlanningMappers(jdbi: Jdbi) {
  jdbi.registerRowMapper(Space::class.java, SpaceRowMapper())
  jdbi.registerRowMapper(Account::class.java, AccountRowMapper())
}

private fun accountSelect(where: String): String =
  """
    SELECT
      accounts.id,
      accounts.space_id,
      accounts.name,
      accounts.category,
      accounts.balance_cents,
      accounts.monthly_contribution_cents,
      accounts.currency,
      accounts.note,
      accounts.balance_updated_at,
      accounts.created_at,
      accounts.updated_at,
      account_displays.initials,
      account_displays.color,
      account_displays.type_label,
      account_displays.subtitle
    FROM accounts
    LEFT JOIN account_displays ON account_displays.account_id = accounts.id
    $where
  """.trimIndent()
