package com.jordi9.doscomas.fixture

import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.jdbi
import com.jordi9.krat.jdbi.handleSync
import org.jdbi.v3.core.kotlin.mapTo

object SpaceTable {

  fun insert(space: SpaceExample): SpaceRow {
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
      ).bind("id", example.id.value)
        .bind("spaceId", example.spaceId.value)
        .bind("name", example.name)
        .bind("category", example.category.apiValue)
        .bind("balanceCents", example.balance.cents)
        .bind("monthlyContributionCents", example.monthlyContribution.cents)
        .bind("currency", example.currency)
        .bind("note", example.note)
        .bind("balanceUpdatedAt", example.balanceUpdatedAt.toEpochMilli())
        .bind("createdAt", example.createdAt.toEpochMilli())
        .bind("updatedAt", example.updatedAt.toEpochMilli())
        .execute()

      if (!example.display.isEmpty()) {
        createUpdate(
          """
            INSERT INTO account_displays (account_id, initials, color, type_label, subtitle)
            VALUES (:accountId, :initials, :color, :typeLabel, :subtitle)
          """.trimIndent()
        ).bind("accountId", example.id.value)
          .bind("initials", example.display.initials)
          .bind("color", example.display.color)
          .bind("typeLabel", example.display.typeLabel)
          .bind("subtitle", example.display.subtitle)
          .execute()
      }
    }
    return AccountRow(id = example.id, spaceId = example.spaceId, name = example.name)
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

data class SpaceRow(val id: SpaceId, val name: String)

data class AccountRow(val id: AccountId, val spaceId: SpaceId, val name: String)
