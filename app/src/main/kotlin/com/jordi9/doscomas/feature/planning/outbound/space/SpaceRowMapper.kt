package com.jordi9.doscomas.feature.planning.outbound.space

import com.jordi9.doscomas.feature.planning.domain.Space
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.Instant

class SpaceRowMapper : RowMapper<Space> {
  override fun map(rs: ResultSet, ctx: StatementContext): Space = Space(
    id = SpaceId(rs.getString("id")),
    name = rs.getString("name"),
    createdAt = Instant.ofEpochMilli(rs.getLong("created_at")),
    updatedAt = Instant.ofEpochMilli(rs.getLong("updated_at"))
  )
}
