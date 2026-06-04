package com.jordi9.doscomas.feature.planning.outbound

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountCategory
import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.Money
import com.jordi9.doscomas.feature.planning.domain.Space
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.krat.jdbi.handle
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

    saveDisplay(account)
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
    createQuery(accountSelect("WHERE accounts.id = :id"))
      .bind("id", accountId.value)
      .mapTo<Account>()
      .findOne()
      .orElse(null)
  }

  suspend fun update(account: Account): Account = jdbi.handle {
    createUpdate(
      """
        UPDATE accounts
        SET name = :name,
            category = :category,
            balance_cents = :balanceCents,
            monthly_contribution_cents = :monthlyContributionCents,
            currency = :currency,
            note = :note,
            balance_updated_at = :balanceUpdatedAt,
            updated_at = :updatedAt
        WHERE id = :id AND space_id = :spaceId
      """.trimIndent()
    ).bindAccount(account)
      .execute()

    saveDisplay(account)
    createQuery(accountSelect("WHERE accounts.id = :id AND accounts.space_id = :spaceId"))
      .bind("id", account.id.value)
      .bind("spaceId", account.spaceId.value)
      .mapTo<Account>()
      .one()
  }

  private fun org.jdbi.v3.core.Handle.saveDisplay(account: Account) {
    if (account.display.isEmpty()) {
      createUpdate("DELETE FROM account_displays WHERE account_id = :accountId")
        .bind("accountId", account.id.value)
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
    ).bind("accountId", account.id.value)
      .bind("initials", account.display.initials)
      .bind("color", account.display.color)
      .bind("typeLabel", account.display.typeLabel)
      .bind("subtitle", account.display.subtitle)
      .execute()
  }

  private fun <T : SqlStatement<T>> T.bindAccount(account: Account): T = bind("id", account.id.value)
    .bind("spaceId", account.spaceId.value)
    .bind("name", account.name)
    .bind("category", account.category.apiValue)
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
      category = AccountCategory.fromApi(rs.getString("category")),
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
