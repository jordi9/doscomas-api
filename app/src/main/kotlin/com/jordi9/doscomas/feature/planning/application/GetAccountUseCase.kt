package com.jordi9.doscomas.feature.planning.application

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.PlanningResourceNotFoundException
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.feature.planning.outbound.AccountRepository

class GetAccountUseCase(
  private val accounts: AccountRepository
) {
  suspend operator fun invoke(spaceId: SpaceId, accountId: AccountId): Account =
    accounts.findByIdAndSpaceId(accountId, spaceId)
      ?: throw PlanningResourceNotFoundException("Account not found: ${accountId.value}")
}

fun GetAccountUseCase(registry: Registry) = GetAccountUseCase(
  accounts = AccountRepository(registry)
)
