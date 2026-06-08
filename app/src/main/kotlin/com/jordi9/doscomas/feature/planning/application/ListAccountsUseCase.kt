package com.jordi9.doscomas.feature.planning.application

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.feature.planning.outbound.account.AccountRepository
import com.jordi9.doscomas.feature.planning.outbound.space.SpaceRepository
import com.jordi9.doscomas.shared.domain.NotFoundException

class ListAccountsUseCase(
  private val spaces: SpaceRepository,
  private val accounts: AccountRepository
) {
  suspend operator fun invoke(spaceId: SpaceId): List<Account> {
    if (!spaces.exists(spaceId)) {
      throw NotFoundException("Space not found: ${spaceId.value}")
    }
    return accounts.findBySpaceId(spaceId)
  }
}

fun ListAccountsUseCase(registry: Registry) = ListAccountsUseCase(
  spaces = SpaceRepository(registry),
  accounts = AccountRepository(registry)
)
