package com.jordi9.doscomas.feature.planning.outbound.space

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Space
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.krat.jdbi.handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo

class SpaceRepository(
  private val jdbi: Jdbi
) {
  suspend fun findAll(): List<Space> = jdbi.handle {
    createQuery("SELECT * FROM spaces ORDER BY created_at DESC")
      .mapTo<Space>()
      .list()
  }

  suspend fun findById(id: SpaceId): Space? = jdbi.handle {
    createQuery("SELECT * FROM spaces WHERE id = :id")
      .bind("id", id.value)
      .mapTo<Space>()
      .findOne()
      .orElse(null)
  }

  suspend fun exists(id: SpaceId): Boolean = jdbi.handle {
    createQuery("SELECT COUNT(*) FROM spaces WHERE id = :id")
      .bind("id", id.value)
      .mapTo<Int>()
      .one() > 0
  }

  suspend fun save(space: Space): Space = jdbi.handle {
    createQuery(
      """
        INSERT INTO spaces (id, name, created_at, updated_at)
        VALUES (:id, :name, :createdAt, :updatedAt)
        RETURNING *
      """.trimIndent()
    ).bind("id", space.id.value)
      .bind("name", space.name.value)
      .bind("createdAt", space.createdAt.toEpochMilli())
      .bind("updatedAt", space.updatedAt.toEpochMilli())
      .mapTo<Space>()
      .one()
  }
}

fun SpaceRepository(registry: Registry) = SpaceRepository(
  jdbi = registry.jdbi
)
