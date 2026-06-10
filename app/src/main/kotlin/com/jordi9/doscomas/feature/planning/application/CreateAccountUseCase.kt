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
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.feature.planning.domain.validNote
import com.jordi9.doscomas.feature.planning.outbound.account.AccountRepository
import com.jordi9.doscomas.feature.planning.outbound.space.SpaceRepository
import com.jordi9.doscomas.shared.domain.NanoIds
import com.jordi9.doscomas.shared.domain.NotFoundException
import com.jordi9.krat.time.TimeClock

class CreateAccountUseCase(
  private val spaces: SpaceRepository,
  private val accounts: AccountRepository,
  private val ids: NanoIds,
  private val clock: TimeClock
) {
  suspend operator fun invoke(request: CreateAccountRequest): Account {
    if (!spaces.exists(request.spaceId)) {
      throw NotFoundException("Space not found: ${request.spaceId.value}")
    }

    val now = clock.now()
    return accounts.save(
      Account(
        id = AccountId(ids.get(AccountId.PREFIX)),
        spaceId = request.spaceId,
        name = request.name,
        category = request.category,
        balance = request.balance,
        monthlyContribution = request.monthlyContribution,
        currency = request.currency,
        note = request.note?.let(::validNote),
        createdAt = now,
        updatedAt = now,
        display = request.display
      )
    )
  }
}

data class CreateAccountRequest(
  val spaceId: SpaceId,
  val name: AccountName,
  val category: AccountCategory,
  val balance: Balance,
  val monthlyContribution: MonthlyContribution,
  val currency: Currency,
  val note: String?,
  val display: AccountDisplay
)

fun CreateAccountUseCase(registry: Registry) = CreateAccountUseCase(
  spaces = SpaceRepository(registry),
  accounts = AccountRepository(registry),
  ids = registry.nanoIds,
  clock = registry.timeClock
)
