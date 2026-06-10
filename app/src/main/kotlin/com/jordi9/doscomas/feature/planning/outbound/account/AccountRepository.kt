package com.jordi9.doscomas.feature.planning.outbound.account

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.krat.jdbi.handle
import org.intellij.lang.annotations.Language
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.bindKotlin
import org.jdbi.v3.core.kotlin.mapTo

class AccountRepository(
  private val jdbi: Jdbi
) {
  suspend fun save(account: Account): Account = jdbi.handle {
    createQuery(INSERT_ACCOUNT)
      .bindKotlin(account.toRecord())
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

  suspend fun update(account: Account): Account? = jdbi.handle {
    createQuery(UPDATE_ACCOUNT)
      .bindKotlin(account.toRecord())
      .mapTo<Account>()
      .findOne()
      .orElse(null)
  }
}

fun AccountRepository(registry: Registry) = AccountRepository(
  jdbi = registry.jdbi
)

@Language("SQL")
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
      accounts.display_json,
      accounts.created_at,
      accounts.updated_at
    FROM accounts
    $where
  """.trimIndent()
