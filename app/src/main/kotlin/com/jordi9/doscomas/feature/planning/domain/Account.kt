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
  val createdAt: Instant,
  val updatedAt: Instant,
  val display: AccountDisplay
)
