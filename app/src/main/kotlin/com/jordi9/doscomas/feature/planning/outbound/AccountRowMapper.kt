package com.jordi9.doscomas.feature.planning.outbound

import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.Money
import com.jordi9.doscomas.feature.planning.domain.Space
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.Instant

class AccountRowMapper : RowMapper<Account> {
  override fun map(rs: ResultSet, ctx: StatementContext): Account {
    val display = AccountDisplay(
      initials = rs.getString("initials"),
      color = rs.getString("color"),
      typeLabel = rs.getString("type_label"),
      subtitle = rs.getString("subtitle")
    )

    return Account(
      id = AccountId(rs.getString("id")),
      spaceId = SpaceId(rs.getString("space_id")),
      name = rs.getString("name"),
      category = AccountCategoryMapper.toDomain(rs.getString("category")),
      balance = Money(rs.getLong("balance_cents")),
      monthlyContribution = Money(rs.getLong("monthly_contribution_cents")),
      currency = rs.getString("currency"),
      note = rs.getString("note"),
      createdAt = Instant.ofEpochMilli(rs.getLong("created_at")),
      updatedAt = Instant.ofEpochMilli(rs.getLong("updated_at")),
      display = display
    )
  }
}

internal fun registerPlanningMappers(jdbi: Jdbi) {
  jdbi.registerRowMapper(Space::class.java, SpaceRowMapper())
  jdbi.registerRowMapper(Account::class.java, AccountRowMapper())
}
