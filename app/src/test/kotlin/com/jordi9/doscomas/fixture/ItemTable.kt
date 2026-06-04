package com.jordi9.doscomas.fixture

import com.jordi9.doscomas.feature.item.domain.ItemId
import com.jordi9.doscomas.jdbi
import com.jordi9.krat.jdbi.handleSync
import org.jdbi.v3.core.kotlin.mapTo

data class ItemRow(val id: ItemId)

object ItemTable {
  fun insert(example: ItemExample): ItemRow {
    val id =
      jdbi().handleSync {
        createQuery(
          """
                    INSERT INTO items (name, description, created_at, updated_at)
                    VALUES (:name, :description, :createdAt, :updatedAt)
                    RETURNING id
          """.trimIndent()
        ).bind("name", example.name)
          .bind("description", example.description)
          .bind("createdAt", example.createdAt.toEpochMilli())
          .bind("updatedAt", example.updatedAt.toEpochMilli())
          .mapTo<Long>()
          .one()
      }
    return ItemRow(id = ItemId(id))
  }

  fun deleteAll() {
    jdbi().handleSync {
      execute("DELETE FROM items")
    }
  }
}
