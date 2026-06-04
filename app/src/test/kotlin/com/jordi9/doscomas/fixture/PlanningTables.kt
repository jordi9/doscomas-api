package com.jordi9.doscomas.fixture

import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.jdbi
import com.jordi9.krat.jdbi.handleSync
import org.jdbi.v3.core.kotlin.mapTo

data class SpaceRow(
  val id: SpaceId,
  val name: String
)

data class AccountRow(
  val id: AccountId,
  val spaceId: SpaceId,
  val name: String
)

fun insertSpace(name: String = "Planning Space", build: SpaceExample.() -> Unit = {}): SpaceRow = SpaceTable.insert(
  SpaceExample(name = name).apply(build)
)

fun insertAccount(spaceId: SpaceId, name: String = "Cash", build: AccountExample.() -> Unit = {}): AccountRow =
  AccountTable.insert(
    AccountExample(spaceId = spaceId, name = name).apply(build)
  )

fun insertAccount(space: SpaceRow, name: String = "Cash", build: AccountExample.() -> Unit = {}): AccountRow =
  insertAccount(
    spaceId = space.id,
    name = name,
    build = build
  )

object SpaceTable {
  fun insert(example: SpaceExample): SpaceRow {
    val space = example.domain()
    jdbi().handleSync {
      createUpdate(
        """
          INSERT INTO spaces (id, name, created_at, updated_at)
          VALUES (:id, :name, :createdAt, :updatedAt)
        """.trimIndent()
      ).bind("id", space.id.value)
        .bind("name", space.name)
        .bind("createdAt", space.createdAt.toEpochMilli())
        .bind("updatedAt", space.updatedAt.toEpochMilli())
        .execute()
    }
    return SpaceRow(id = space.id, name = space.name)
  }

  fun deleteAll() {
    jdbi().handleSync {
      execute("DELETE FROM spaces")
    }
  }
}

object AccountTable {
  fun insert(example: AccountExample): AccountRow {
    val account = example.domain()
    jdbi().handleSync {
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
      ).bind("id", account.id.value)
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
        .execute()

      if (!account.display.isEmpty()) {
        createUpdate(
          """
            INSERT INTO account_displays (account_id, initials, color, type_label, subtitle)
            VALUES (:accountId, :initials, :color, :typeLabel, :subtitle)
          """.trimIndent()
        ).bind("accountId", account.id.value)
          .bind("initials", account.display.initials)
          .bind("color", account.display.color)
          .bind("typeLabel", account.display.typeLabel)
          .bind("subtitle", account.display.subtitle)
          .execute()
      }
    }
    return AccountRow(id = account.id, spaceId = account.spaceId, name = account.name)
  }

  fun displayExists(accountId: AccountId): Boolean = jdbi().handleSync {
    createQuery("SELECT COUNT(*) FROM account_displays WHERE account_id = :accountId")
      .bind("accountId", accountId.value)
      .mapTo<Int>()
      .one() > 0
  }

  fun deleteAll() {
    jdbi().handleSync {
      execute("DELETE FROM account_displays")
      execute("DELETE FROM accounts")
    }
  }
}
