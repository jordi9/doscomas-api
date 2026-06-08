package com.jordi9.doscomas.fixture

import com.jordi9.doscomas.feature.planning.domain.AccountCategory
import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.Money
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.sharedClock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.time.Instant
import java.util.UUID

data class SpaceExample(
  val id: SpaceId = spaceId(),
  val name: String = "FIRE",
  val createdAt: Instant = sharedClock().now(),
  val updatedAt: Instant = createdAt
)

data class AccountExample(
  val spaceId: SpaceId,
  val id: AccountId = accountId(),
  val name: String = "Cash",
  val category: AccountCategory = AccountCategory.CASH,
  val balance: Money = Money(100_00),
  val monthlyContribution: Money = Money(0),
  val currency: String = "EUR",
  val note: String? = null,
  val createdAt: Instant = sharedClock().now(),
  val updatedAt: Instant = createdAt,
  val display: AccountDisplay = accountDisplay()
)

fun spaceId(): SpaceId = SpaceId("sp_${suffix()}")

fun accountId(): AccountId = AccountId("acc_${suffix()}")

fun accountDisplay(json: String = "{}"): AccountDisplay = AccountDisplay(Json.parseToJsonElement(json).jsonObject)

private fun suffix(): String = UUID.randomUUID().toString().replace("-", "").take(21)
