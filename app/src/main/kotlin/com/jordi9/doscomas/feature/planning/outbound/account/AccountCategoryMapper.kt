package com.jordi9.doscomas.feature.planning.outbound.account

import com.jordi9.doscomas.feature.planning.domain.AccountCategory

internal object AccountCategoryMapper {
  fun toDatabase(category: AccountCategory): String = when (category) {
    AccountCategory.CASH -> "cash"
    AccountCategory.INVESTMENT -> "investment"
    AccountCategory.PRIVATE_PENSION -> "private_pension"
    AccountCategory.REAL_ESTATE -> "real_estate"
    AccountCategory.SOCIAL_SECURITY -> "social_security"
    AccountCategory.OTHER -> "other"
  }

  fun toDomain(value: String): AccountCategory = when (value) {
    "cash" -> AccountCategory.CASH
    "investment" -> AccountCategory.INVESTMENT
    "private_pension" -> AccountCategory.PRIVATE_PENSION
    "real_estate" -> AccountCategory.REAL_ESTATE
    "social_security" -> AccountCategory.SOCIAL_SECURITY
    "other" -> AccountCategory.OTHER
    else -> error("Unknown account category in database: $value")
  }
}
