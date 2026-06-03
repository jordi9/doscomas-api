package com.jordi9.doscomas.feature.planning.application

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.PlanningResourceNotFoundException
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.feature.planning.outbound.AccountRepository
import com.jordi9.doscomas.feature.planning.outbound.SpaceRepository

class ListAccountsUseCase(
  private val spaces: SpaceRepository,
  private val accounts: AccountRepository
) {
  suspend operator fun invoke(spaceId: SpaceId): List<Account> {
    if (!spaces.exists(spaceId)) {
      throw PlanningResourceNotFoundException("Space not found: ${spaceId.value}")
    }
    return accounts.findBySpaceId(spaceId)
  }
}

fun ListAccountsUseCase(registry: Registry) = ListAccountsUseCase(
  spaces = SpaceRepository(registry),
  accounts = AccountRepository(registry)
)
