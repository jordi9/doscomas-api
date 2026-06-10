package com.jordi9.doscomas.feature.planning.domain

import java.time.Instant

data class Account(
  val id: AccountId,
  val spaceId: SpaceId,
  val name: AccountName,
  val category: AccountCategory,
  val balance: Balance,
  val monthlyContribution: MonthlyContribution,
  val currency: Currency,
  val note: String?,
  val createdAt: Instant,
  val updatedAt: Instant,
  val display: AccountDisplay
)
