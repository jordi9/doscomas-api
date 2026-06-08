package com.jordi9.doscomas.feature.planning.outbound

import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.Space
import com.jordi9.doscomas.feature.planning.outbound.account.AccountRowMapper
import com.jordi9.doscomas.feature.planning.outbound.space.SpaceRowMapper
import org.jdbi.v3.core.Jdbi

internal fun registerPlanningMappers(jdbi: Jdbi) {
  jdbi.registerRowMapper(Space::class.java, SpaceRowMapper())
  jdbi.registerRowMapper(Account::class.java, AccountRowMapper())
}
