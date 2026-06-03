package com.jordi9.doscomas.fixture

import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.jdbi
import com.jordi9.krat.jdbi.handleSync
import org.jdbi.v3.core.kotlin.mapTo

private const val TEST_TIME = 946684800000L

fun spaceId(suffix: Char): SpaceId = SpaceId("sp_${suffix.toString().repeat(21)}")

fun accountId(suffix: Char): AccountId = AccountId("acc_${suffix.toString().repeat(21)}")

data class SpaceRow(
  val id: SpaceId,
  val name: String
)

data class AccountRow(
  val id: AccountId,
  val spaceId: SpaceId,
  val name: String
)

object SpaceTable {
  fun insert(
    id: SpaceId = spaceId('a'),
    name: String = "Planning Space",
    createdAt: Long = TEST_TIME,
    updatedAt: Long = TEST_TIME
  ): SpaceRow {
    jdbi().handleSync {
      createUpdate(
        """
          INSERT INTO spaces (id, name, created_at, updated_at)
          VALUES (:id, :name, :createdAt, :updatedAt)
        """.trimIndent()
      ).bind("id", id.value)
        .bind("name", name)
        .bind("createdAt", createdAt)
        .bind("updatedAt", updatedAt)
        .execute()
    }
    return SpaceRow(id = id, name = name)
  }

  fun deleteAll() {
    jdbi().handleSync {
      execute("DELETE FROM spaces")
    }
  }
}

object AccountTable {
  fun insert(
    id: AccountId = accountId('a'),
    spaceId: SpaceId,
    name: String = "Cash",
    category: String = "cash",
    balanceCents: Long = 100_00,
    monthlyContributionCents: Long = 0,
    currency: String = "EUR",
    note: String? = null,
    balanceUpdatedAt: Long = TEST_TIME,
    createdAt: Long = TEST_TIME,
    updatedAt: Long = TEST_TIME,
    display: AccountDisplay = AccountDisplay()
  ): AccountRow {
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
      ).bind("id", id.value)
        .bind("spaceId", spaceId.value)
        .bind("name", name)
        .bind("category", category)
        .bind("balanceCents", balanceCents)
        .bind("monthlyContributionCents", monthlyContributionCents)
        .bind("currency", currency)
        .bind("note", note)
        .bind("balanceUpdatedAt", balanceUpdatedAt)
        .bind("createdAt", createdAt)
        .bind("updatedAt", updatedAt)
        .execute()

      if (!display.isEmpty()) {
        createUpdate(
          """
            INSERT INTO account_displays (account_id, initials, color, type_label, subtitle)
            VALUES (:accountId, :initials, :color, :typeLabel, :subtitle)
          """.trimIndent()
        ).bind("accountId", id.value)
          .bind("initials", display.initials)
          .bind("color", display.color)
          .bind("typeLabel", display.typeLabel)
          .bind("subtitle", display.subtitle)
          .execute()
      }
    }
    return AccountRow(id = id, spaceId = spaceId, name = name)
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
