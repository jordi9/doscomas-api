package com.jordi9.doscomas.feature.planning.inbound.account

import com.jordi9.doscomas.feature.planning.domain.AccountCategory
import com.jordi9.doscomas.feature.planning.inbound.badRequest

internal fun toAccountCategory(value: String): AccountCategory = when (value) {
  "cash" -> AccountCategory.CASH
  "investment" -> AccountCategory.INVESTMENT
  "private_pension" -> AccountCategory.PRIVATE_PENSION
  "real_estate" -> AccountCategory.REAL_ESTATE
  "social_security" -> AccountCategory.SOCIAL_SECURITY
  "other" -> AccountCategory.OTHER
  else -> badRequest("Invalid account category")
}

internal fun AccountCategory.toResponse(): String = when (this) {
  AccountCategory.CASH -> "cash"
  AccountCategory.INVESTMENT -> "investment"
  AccountCategory.PRIVATE_PENSION -> "private_pension"
  AccountCategory.REAL_ESTATE -> "real_estate"
  AccountCategory.SOCIAL_SECURITY -> "social_security"
  AccountCategory.OTHER -> "other"
}
