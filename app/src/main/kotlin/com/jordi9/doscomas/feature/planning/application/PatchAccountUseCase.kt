package com.jordi9.doscomas.feature.planning.application

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountChanges
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.DisplayChange
import com.jordi9.doscomas.feature.planning.domain.NullableField
import com.jordi9.doscomas.feature.planning.domain.PlanningResourceNotFoundException
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.feature.planning.domain.validBalance
import com.jordi9.doscomas.feature.planning.domain.validColor
import com.jordi9.doscomas.feature.planning.domain.validCurrency
import com.jordi9.doscomas.feature.planning.domain.validDisplayText
import com.jordi9.doscomas.feature.planning.domain.validInitials
import com.jordi9.doscomas.feature.planning.domain.validName
import com.jordi9.doscomas.feature.planning.domain.validNote
import com.jordi9.doscomas.feature.planning.outbound.AccountRepository
import com.jordi9.krat.time.TimeClock

class PatchAccountUseCase(
  private val accounts: AccountRepository,
  private val clock: TimeClock
) {
  suspend operator fun invoke(spaceId: SpaceId, accountId: AccountId, changes: AccountChanges): Account {
    val account = accounts.findByIdAndSpaceId(accountId, spaceId)
      ?: throw PlanningResourceNotFoundException("Account not found: ${accountId.value}")

    val updated = account.apply(changes.validated(), clock.now())
    return accounts.update(updated)
  }
}

private fun AccountChanges.validated(): AccountChanges = copy(
  name = name?.let(::validName),
  balance = balance?.let(::validBalance),
  currency = currency?.let(::validCurrency),
  note = note.map(::validNote),
  display = display.validated()
)

private fun DisplayChange.validated(): DisplayChange = when (this) {
  DisplayChange.Unchanged -> this

  DisplayChange.Clear -> this

  is DisplayChange.Update -> copy(
    initials = initials.map(::validInitials),
    color = color.map(::validColor),
    typeLabel = typeLabel.map { validDisplayText("Type label", it) },
    subtitle = subtitle.map { validDisplayText("Subtitle", it) }
  )
}

private fun <T, R> NullableField<T>.map(transform: (T) -> R): NullableField<R> = when (this) {
  NullableField.Unchanged -> NullableField.Unchanged
  NullableField.Clear -> NullableField.Clear
  is NullableField.Set -> NullableField.Set(transform(value))
}

fun PatchAccountUseCase(registry: Registry) = PatchAccountUseCase(
  accounts = AccountRepository(registry),
  clock = registry.timeClock
)
