package com.jordi9.doscomas.fixture

import com.jordi9.doscomas.feature.planning.domain.Account
import com.jordi9.doscomas.feature.planning.domain.AccountCategory
import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.Money
import com.jordi9.doscomas.feature.planning.domain.Space
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import java.time.Instant
import java.util.UUID

private val TEST_INSTANT = Instant.parse("2000-01-01T00:00:00Z")

object ExampleIds {
  fun spaceId(): SpaceId = SpaceId("sp_${suffix()}")

  fun accountId(): AccountId = AccountId("acc_${suffix()}")

  private fun suffix(): String = UUID.randomUUID().toString().replace("-", "").take(21)
}

class SpaceExample(
  var id: SpaceId = ExampleIds.spaceId(),
  var name: String = "Planning Space",
  var createdAt: Instant = TEST_INSTANT,
  var updatedAt: Instant = TEST_INSTANT
) {
  fun domain() = Space(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt
  )
}

class AccountExample(
  var spaceId: SpaceId,
  var id: AccountId = ExampleIds.accountId(),
  var name: String = "Cash",
  var category: AccountCategory = AccountCategory.CASH,
  var balance: Money = Money(100_00),
  var monthlyContribution: Money = Money(0),
  var currency: String = "EUR",
  var note: String? = null,
  var balanceUpdatedAt: Instant = TEST_INSTANT,
  var createdAt: Instant = TEST_INSTANT,
  var updatedAt: Instant = TEST_INSTANT,
  var display: AccountDisplay = AccountDisplay()
) {
  fun domain() = Account(
    id = id,
    spaceId = spaceId,
    name = name,
    category = category,
    balance = balance,
    monthlyContribution = monthlyContribution,
    currency = currency,
    note = note,
    balanceUpdatedAt = balanceUpdatedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    display = display
  )
}
