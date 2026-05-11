package com.jordi9.doscomas.feature.item.outbound

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.item.domain.Item
import com.jordi9.doscomas.feature.item.domain.ItemId
import com.jordi9.krat.jdbi.handle
import com.jordi9.krat.time.TimeClock
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.Instant

class ItemRepository(
  private val jdbi: Jdbi,
  private val clock: TimeClock
) {
  suspend fun findAll(): List<Item> = jdbi.handle {
    createQuery("SELECT * FROM items ORDER BY created_at DESC")
      .mapTo<Item>()
      .list()
  }

  suspend fun findById(id: ItemId): Item? = jdbi.handle {
    createQuery("SELECT * FROM items WHERE id = :id")
      .bind("id", id.value)
      .mapTo<Item>()
      .findOne()
      .orElse(null)
  }

  suspend fun save(name: String, description: String?): Item = jdbi.handle {
    val now = clock.now().toEpochMilli()
    createQuery(
      """
                INSERT INTO items (name, description, created_at, updated_at)
                VALUES (:name, :description, :now, :now)
                RETURNING *
      """.trimIndent()
    ).bind("name", name)
      .bind("description", description)
      .bind("now", now)
      .mapTo<Item>()
      .one()
  }

  suspend fun deleteAll() = jdbi.handle {
    execute("DELETE FROM items")
  }
}

class ItemRowMapper : RowMapper<Item> {
  override fun map(rs: ResultSet, ctx: StatementContext): Item = Item(
    id = ItemId(rs.getLong("id")),
    name = rs.getString("name"),
    description = rs.getString("description"),
    createdAt = Instant.ofEpochMilli(rs.getLong("created_at")),
    updatedAt = Instant.ofEpochMilli(rs.getLong("updated_at"))
  )
}

fun ItemRepository(registry: Registry) = ItemRepository(
  jdbi = registry.jdbi,
  clock = registry.timeClock
)

internal fun registerItemMappers(jdbi: Jdbi) {
  jdbi.registerRowMapper(Item::class.java, ItemRowMapper())
}
