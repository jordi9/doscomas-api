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

    fun <T> choose(current: T, update: FieldUpdate<T>): T = when (update) {
      FieldUpdate.Keep -> current

      is FieldUpdate.Replace -> {
        if (update.value != current) {
          changed = true
          update.value
        } else {
          current
        }
      }
    }

    next = next.copy(
      name = choose(next.name, changes.name),
      category = choose(next.category, changes.category),
      monthlyContribution = choose(next.monthlyContribution, changes.monthlyContribution),
      currency = choose(next.currency, changes.currency)
    )

    when (val balanceUpdate = changes.balance) {
      FieldUpdate.Keep -> Unit

      is FieldUpdate.Replace -> {
        if (balanceUpdate.value != next.balance) {
          changed = true
          next = next.copy(balance = balanceUpdate.value, balanceUpdatedAt = now)
        }
      }
    }

    when (val noteUpdate = changes.note) {
      FieldUpdate.Keep -> Unit

      is FieldUpdate.Replace -> {
        if (next.note != noteUpdate.value) {
          changed = true
          next = next.copy(note = noteUpdate.value)
        }
      }
    }

    when (val displayUpdate = changes.display) {
      FieldUpdate.Keep -> Unit

      is FieldUpdate.Replace -> {
        val display = displayUpdate.value ?: AccountDisplay()
        if (next.display != display) {
          changed = true
          next = next.copy(display = display)
        }
      }
    }

    return if (changed) next.copy(updatedAt = now) else next
  }
}

data class AccountChanges(
  val name: FieldUpdate<String>,
  val category: FieldUpdate<AccountCategory>,
  val balance: FieldUpdate<Money>,
  val monthlyContribution: FieldUpdate<Money>,
  val currency: FieldUpdate<String>,
  val note: FieldUpdate<String?>,
  val display: FieldUpdate<AccountDisplay?>
)
