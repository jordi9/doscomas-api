package com.jordi9.doscomas.fixture

import com.jordi9.doscomas.feature.item.domain.ItemId
import com.jordi9.doscomas.jdbi
import com.jordi9.krat.jdbi.handleSync
import org.jdbi.v3.core.kotlin.mapTo

data class ItemRow(
  val id: ItemId,
  val name: String,
  val description: String?
)

object ItemTable {
  fun insert(
    name: String = "Test Item",
    description: String? = null,
    createdAt: Long = 1136214245000,
    updatedAt: Long = 1136214245000
  ): ItemRow {
    val id =
      jdbi().handleSync {
        createQuery(
          """
                    INSERT INTO items (name, description, created_at, updated_at)
                    VALUES (:name, :description, :createdAt, :updatedAt)
                    RETURNING id
          """.trimIndent()
        ).bind("name", name)
          .bind("description", description)
          .bind("createdAt", createdAt)
          .bind("updatedAt", updatedAt)
          .mapTo<Long>()
          .one()
      }
    return ItemRow(id = ItemId(id), name = name, description = description)
  }

  fun findById(id: ItemId): ItemRow? = jdbi().handleSync {
    createQuery("SELECT id, name, description FROM items WHERE id = :id")
      .bind("id", id.value)
      .map { rs, _ -> ItemRow(ItemId(rs.getLong("id")), rs.getString("name"), rs.getString("description")) }
      .findOne()
      .orElse(null)
  }

  fun deleteAll() {
    jdbi().handleSync {
      execute("DELETE FROM items")
    }
  }
}
