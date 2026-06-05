package com.jordi9.doscomas.feature.planning.application

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountCategory
import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.Money
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.feature.planning.domain.validBalance
import com.jordi9.doscomas.feature.planning.domain.validCurrency
import com.jordi9.doscomas.feature.planning.domain.validDisplay
import com.jordi9.doscomas.feature.planning.domain.validName
import com.jordi9.doscomas.feature.planning.domain.validNote
import com.jordi9.doscomas.feature.planning.outbound.AccountRepository
import com.jordi9.doscomas.feature.planning.outbound.SpaceRepository
import com.jordi9.doscomas.shared.domain.NanoIds
import com.jordi9.doscomas.shared.domain.NotFoundException
import com.jordi9.krat.time.TimeClock

class CreateAccountUseCase(
  private val spaces: SpaceRepository,
  private val accounts: AccountRepository,
  private val ids: NanoIds,
  private val clock: TimeClock
) {
  suspend operator fun invoke(command: CreateAccountCommand): Account {
    if (!spaces.exists(command.spaceId)) {
      throw NotFoundException("Space not found: ${command.spaceId.value}")
    }

    val now = clock.now()
    return accounts.save(
      Account(
        id = AccountId(ids.get(AccountId.PREFIX)),
        spaceId = command.spaceId,
        name = validName(command.name),
        category = command.category,
        balance = validBalance(command.balance),
        monthlyContribution = command.monthlyContribution,
        currency = validCurrency(command.currency),
        note = command.note?.let(::validNote),
        balanceUpdatedAt = now,
        createdAt = now,
        updatedAt = now,
        display = validDisplay(command.display)
      )
    )
  }
}

data class CreateAccountCommand(
  val spaceId: SpaceId,
  val name: String,
  val category: AccountCategory,
  val balance: Money,
  val monthlyContribution: Money,
  val currency: String,
  val note: String?,
  val display: AccountDisplay
)

fun CreateAccountUseCase(registry: Registry) = CreateAccountUseCase(
  spaces = SpaceRepository(registry),
  accounts = AccountRepository(registry),
  ids = registry.nanoIds,
  clock = registry.timeClock
)
