package com.jordi9.doscomas.feature.planning.inbound

import com.jordi9.doscomas.feature.planning.domain.AccountCategory

internal fun toAccountCategory(value: String): AccountCategory = when (value) {
  "cash" -> AccountCategory.CASH
  "investment" -> AccountCategory.INVESTMENT
  "private_pension" -> AccountCategory.PRIVATE_PENSION
  "real_estate" -> AccountCategory.REAL_ESTATE
  "social_security" -> AccountCategory.SOCIAL_SECURITY
  "other" -> AccountCategory.OTHER
  else -> throw IllegalArgumentException("Invalid account category")
}
