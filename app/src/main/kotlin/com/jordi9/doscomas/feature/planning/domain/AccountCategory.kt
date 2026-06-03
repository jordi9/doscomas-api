package com.jordi9.doscomas.feature.planning.domain

enum class AccountCategory(val apiValue: String) {
  CASH("cash"),
  INVESTMENT("investment"),
  PRIVATE_PENSION("private_pension"),
  REAL_ESTATE("real_estate"),
  SOCIAL_SECURITY("social_security"),
  OTHER("other");

  companion object {
    fun fromApi(value: String): AccountCategory = entries.firstOrNull { it.apiValue == value }
      ?: throw IllegalArgumentException("Invalid account category")
  }
}
