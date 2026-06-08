package com.jordi9.doscomas.feature.planning.application

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountBalanceUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountCategoryUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountCurrencyUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountDisplayUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.AccountMonthlyContributionUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountNameUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountNoteUpdate
import com.jordi9.doscomas.feature.planning.domain.AccountUpdate
import com.jordi9.doscomas.feature.planning.domain.validBalance
import com.jordi9.doscomas.feature.planning.domain.validCurrency
import com.jordi9.doscomas.feature.planning.domain.validDisplay
import com.jordi9.doscomas.feature.planning.domain.validName
import com.jordi9.doscomas.feature.planning.domain.validNote
import com.jordi9.doscomas.feature.planning.outbound.AccountRepository
import com.jordi9.doscomas.shared.domain.NotFoundException
import com.jordi9.krat.time.TimeClock

class UpdateAccountUseCase(
  private val accounts: AccountRepository,
  private val clock: TimeClock
) {
  suspend operator fun invoke(request: UpdateAccountRequest): Account {
    val updates = request.updates.validated()
    if (!accounts.exists(request.accountId)) {
      throw NotFoundException("Account not found: ${request.accountId.value}")
    }

    return accounts.update(request.accountId, updates, clock.now())
      ?: throw NotFoundException("Account not found: ${request.accountId.value}")
  }
}

data class UpdateAccountRequest(
  val accountId: AccountId,
  val updates: List<AccountUpdate>
)

private fun List<AccountUpdate>.validated(): List<AccountUpdate> = map { it.validated() }

private fun AccountUpdate.validated(): AccountUpdate = when (this) {
  is AccountNameUpdate -> copy(value = validName(value))
  is AccountCategoryUpdate -> this
  is AccountBalanceUpdate -> copy(value = validBalance(value))
  is AccountMonthlyContributionUpdate -> this
  is AccountCurrencyUpdate -> copy(value = validCurrency(value))
  is AccountNoteUpdate -> copy(value = value?.let(::validNote))
  is AccountDisplayUpdate -> copy(value = validDisplay(value))
}

fun UpdateAccountUseCase(registry: Registry) = UpdateAccountUseCase(
  accounts = AccountRepository(registry),
  clock = registry.timeClock
)
