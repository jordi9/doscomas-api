package com.jordi9.doscomas.feature.planning.domain

import java.time.Instant

data class Account(
  val id: AccountId,
  val spaceId: SpaceId,
  val name: String,
  val category: AccountCategory,
  val balance: Money,
  val monthlyContribution: Money,
  val currency: String,
  val note: String?,
  val balanceUpdatedAt: Instant,
  val createdAt: Instant,
  val updatedAt: Instant,
  val display: AccountDisplay
) {
  fun apply(changes: AccountChanges, now: Instant): Account {
    var changed = false
    var next = this

    fun <T> choose(current: T, proposed: T?): T = if (proposed != null && proposed != current) {
      changed = true
      proposed
    } else {
      current
    }

    next = next.copy(
      name = choose(next.name, changes.name),
      category = choose(next.category, changes.category),
      monthlyContribution = choose(next.monthlyContribution, changes.monthlyContribution),
      currency = choose(next.currency, changes.currency)
    )

    changes.balance?.let { proposed ->
      if (proposed != next.balance) {
        changed = true
        next = next.copy(balance = proposed, balanceUpdatedAt = now)
      }
    }

    when (val noteChange = changes.note) {
      NullableField.Unchanged -> Unit

      NullableField.Clear -> {
        if (next.note != null) {
          changed = true
          next = next.copy(note = null)
        }
      }

      is NullableField.Set -> {
        if (next.note != noteChange.value) {
          changed = true
          next = next.copy(note = noteChange.value)
        }
      }
    }

    when (val displayChange = changes.display) {
      DisplayChange.Unchanged -> Unit

      DisplayChange.Clear -> {
        if (!next.display.isEmpty()) {
          changed = true
          next = next.copy(display = AccountDisplay())
        }
      }

      is DisplayChange.Update -> {
        val proposed = next.display.apply(displayChange)
        if (proposed != next.display) {
          changed = true
          next = next.copy(display = proposed)
        }
      }
    }

    return if (changed) next.copy(updatedAt = now) else next
  }
}

data class AccountChanges(
  val name: String? = null,
  val category: AccountCategory? = null,
  val balance: Money? = null,
  val monthlyContribution: Money? = null,
  val currency: String? = null,
  val note: NullableField<String> = NullableField.Unchanged,
  val display: DisplayChange = DisplayChange.Unchanged
)

sealed interface NullableField<out T> {
  data object Unchanged : NullableField<Nothing>

  data object Clear : NullableField<Nothing>

  data class Set<T>(val value: T) : NullableField<T>
}

sealed interface DisplayChange {
  data object Unchanged : DisplayChange

  data object Clear : DisplayChange

  data class Update(
    val initials: NullableField<String> = NullableField.Unchanged,
    val color: NullableField<String> = NullableField.Unchanged,
    val typeLabel: NullableField<String> = NullableField.Unchanged,
    val subtitle: NullableField<String> = NullableField.Unchanged
  ) : DisplayChange
}

private fun AccountDisplay.apply(change: DisplayChange.Update): AccountDisplay = copy(
  initials = initials.apply(change.initials),
  color = color.apply(change.color),
  typeLabel = typeLabel.apply(change.typeLabel),
  subtitle = subtitle.apply(change.subtitle)
)

private fun String?.apply(change: NullableField<String>): String? = when (change) {
  NullableField.Unchanged -> this
  NullableField.Clear -> null
  is NullableField.Set -> change.value
}
