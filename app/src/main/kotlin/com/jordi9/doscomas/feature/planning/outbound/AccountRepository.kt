package com.jordi9.doscomas.feature.planning.outbound

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountCoreUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import com.jordi9.doscomas.feature.planning.domain.AccountDisplayUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.AccountUpdate
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.krat.jdbi.handle
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.bindKotlin
import org.jdbi.v3.core.kotlin.mapTo
import java.time.Instant

class AccountRepository(
  private val jdbi: Jdbi
) {
  suspend fun save(account: Account): Account = jdbi.handle {
    createUpdate(INSERT_ACCOUNT)
      .bindKotlin(account.toRecord())
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

  suspend fun exists(accountId: AccountId): Boolean = jdbi.handle {
    createQuery("SELECT COUNT(*) FROM accounts WHERE id = :id")
      .bind("id", accountId.value)
      .mapTo<Int>()
      .one() > 0
  }

  suspend fun update(accountId: AccountId, updates: List<AccountUpdate>, now: Instant): Account? = jdbi.handle {
    inTransaction<Account?, Exception> { transaction ->
      transaction.applyUpdates(accountId, updates, now)
    }
  }

  private fun Handle.applyUpdates(accountId: AccountId, updates: List<AccountUpdate>, now: Instant): Account? {
    updates.forEach { writeUpdate(accountId, it, now) }
    return findAccount(accountId)
  }

  private fun Handle.writeUpdate(accountId: AccountId, update: AccountUpdate, now: Instant) {
    when (update) {
      is AccountCoreUpdate -> update.toWrite().execute(this, accountId, now)
      is AccountDisplayUpdate -> updateDisplay(accountId, update.value, now)
    }
  }

  private fun Handle.updateDisplay(accountId: AccountId, display: AccountDisplay, now: Instant) {
    if (touchAccount(accountId, now) > 0) {
      saveDisplay(accountId, display)
    }
  }

  private fun Handle.saveDisplay(accountId: AccountId, display: AccountDisplay) {
    if (display.isEmpty()) {
      createUpdate(DELETE_DISPLAY)
        .bindKotlin(AccountDisplayIdRecord(accountId.value))
        .execute()
      return
    }

    createUpdate(UPSERT_DISPLAY)
      .bindKotlin(display.toRecord(accountId))
      .execute()
  }

  private fun Handle.touchAccount(accountId: AccountId, now: Instant): Int = createUpdate(TOUCH_ACCOUNT)
    .bindKotlin(TouchAccountRecord(accountId.value, now.toEpochMilli()))
    .execute()

  private fun Handle.findAccount(accountId: AccountId): Account? = createQuery(accountSelect("WHERE accounts.id = :id"))
    .bind("id", accountId.value)
    .mapTo<Account>()
    .findOne()
    .orElse(null)
}

fun AccountRepository(registry: Registry) = AccountRepository(
  jdbi = registry.jdbi
)

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
