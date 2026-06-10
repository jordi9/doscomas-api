package com.jordi9.doscomas.feature.planning.application

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountCategory
import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.AccountName
import com.jordi9.doscomas.feature.planning.domain.Balance
import com.jordi9.doscomas.feature.planning.domain.Currency
import com.jordi9.doscomas.feature.planning.domain.MonthlyContribution
import com.jordi9.doscomas.feature.planning.domain.validNote
import com.jordi9.doscomas.feature.planning.outbound.account.AccountRepository
import com.jordi9.doscomas.shared.domain.NotFoundException
import com.jordi9.krat.time.TimeClock

class UpdateAccountUseCase(
  private val accounts: AccountRepository,
  private val clock: TimeClock
) {
  suspend operator fun invoke(request: UpdateAccountRequest): Account {
    val updates = request.updates.map { it.validated() }
    val current = accounts.findById(request.accountId)
      ?: throw NotFoundException("Account not found: ${request.accountId.value}")

    if (updates.isEmpty()) {
      return current
    }

    val updated = updates
      .fold(current) { account, update -> account.with(update) }
      .copy(updatedAt = clock.now())

    return accounts.update(updated)
      ?: throw NotFoundException("Account not found: ${request.accountId.value}")
  }
}

data class UpdateAccountRequest(
  val accountId: AccountId,
  val updates: List<AccountUpdate>
)

sealed interface AccountUpdate

data class AccountNameUpdate(val value: AccountName) : AccountUpdate

data class AccountCategoryUpdate(val value: AccountCategory) : AccountUpdate

data class AccountBalanceUpdate(val value: Balance) : AccountUpdate

data class AccountMonthlyContributionUpdate(val value: MonthlyContribution) : AccountUpdate

data class AccountCurrencyUpdate(val value: Currency) : AccountUpdate

data class AccountNoteUpdate(val value: String?) : AccountUpdate

data class AccountDisplayUpdate(val value: AccountDisplay) : AccountUpdate

private fun AccountUpdate.validated(): AccountUpdate = when (this) {
  is AccountNameUpdate -> this
  is AccountCategoryUpdate -> this
  is AccountBalanceUpdate -> this
  is AccountMonthlyContributionUpdate -> this
  is AccountCurrencyUpdate -> this
  is AccountNoteUpdate -> copy(value = value?.let(::validNote))
  is AccountDisplayUpdate -> this
}

private fun Account.with(update: AccountUpdate): Account = when (update) {
  is AccountNameUpdate -> copy(name = update.value)
  is AccountCategoryUpdate -> copy(category = update.value)
  is AccountBalanceUpdate -> copy(balance = update.value)
  is AccountMonthlyContributionUpdate -> copy(monthlyContribution = update.value)
  is AccountCurrencyUpdate -> copy(currency = update.value)
  is AccountNoteUpdate -> copy(note = update.value)
  is AccountDisplayUpdate -> copy(display = update.value)
}

fun UpdateAccountUseCase(registry: Registry) = UpdateAccountUseCase(
  accounts = AccountRepository(registry),
  clock = registry.timeClock
)
