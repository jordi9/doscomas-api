package com.jordi9.doscomas.feature.planning.application

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountChanges
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.PlanningResourceNotFoundException
import com.jordi9.doscomas.feature.planning.domain.map
import com.jordi9.doscomas.feature.planning.domain.validBalance
import com.jordi9.doscomas.feature.planning.domain.validCurrency
import com.jordi9.doscomas.feature.planning.domain.validDisplay
import com.jordi9.doscomas.feature.planning.domain.validName
import com.jordi9.doscomas.feature.planning.domain.validNote
import com.jordi9.doscomas.feature.planning.outbound.AccountRepository
import com.jordi9.krat.time.TimeClock

class PatchAccountUseCase(
  private val accounts: AccountRepository,
  private val clock: TimeClock
) {
  suspend operator fun invoke(accountId: AccountId, changes: AccountChanges): Account {
    val account = accounts.findById(accountId)
      ?: throw PlanningResourceNotFoundException("Account not found: ${accountId.value}")

    val updated = account.apply(changes.validated(), clock.now())
    return accounts.update(updated)
  }
}

private fun AccountChanges.validated(): AccountChanges = copy(
  name = name.map(::validName),
  balance = balance.map(::validBalance),
  currency = currency.map(::validCurrency),
  note = note.map { value -> value?.let(::validNote) },
  display = display.map { value -> value?.let(::validDisplay) }
)

fun PatchAccountUseCase(registry: Registry) = PatchAccountUseCase(
  accounts = AccountRepository(registry),
  clock = registry.timeClock
)
