package com.jordi9.doscomas.fixture

import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.feature.planning.outbound.account.AccountCategoryMapper
import com.jordi9.doscomas.jdbi
import com.jordi9.krat.jdbi.handleSync
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
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
            display_json,
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
            :displayJson,
            :createdAt,
            :updatedAt
          )
        """.trimIndent()
      ).bind("id", example.id.value)
        .bind("spaceId", example.spaceId.value)
        .bind("name", example.name)
        .bind("category", AccountCategoryMapper.toDatabase(example.category))
        .bind("balanceCents", example.balance.cents)
        .bind("monthlyContributionCents", example.monthlyContribution.cents)
        .bind("currency", example.currency)
        .bind("note", example.note)
        .bind("displayJson", Json.encodeToString(example.display.value))
        .bind("createdAt", example.createdAt.toEpochMilli())
        .bind("updatedAt", example.updatedAt.toEpochMilli())
        .execute()
    }
    return AccountRow(id = example.id, spaceId = example.spaceId, name = example.name)
  }

  fun displayJson(accountId: AccountId): String = jdbi().handleSync {
    createQuery("SELECT display_json FROM accounts WHERE id = :accountId")
      .bind("accountId", accountId.value)
      .mapTo<String>()
      .one()
  }

  fun deleteAll() {
    jdbi().handleSync {
      execute("DELETE FROM accounts")
    }
  }
}

data class SpaceRow(val id: SpaceId, val name: String)

data class AccountRow(val id: AccountId, val spaceId: SpaceId, val name: String)
