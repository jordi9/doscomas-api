package com.jordi9.doscomas.feature.planning.application

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.outbound.account.AccountRepository
import com.jordi9.doscomas.shared.domain.NotFoundException

class GetAccountUseCase(
  private val accounts: AccountRepository
) {
  suspend operator fun invoke(accountId: AccountId): Account = accounts.findById(accountId)
    ?: throw NotFoundException("Account not found: ${accountId.value}")
}

fun GetAccountUseCase(registry: Registry) = GetAccountUseCase(
  accounts = AccountRepository(registry)
)
