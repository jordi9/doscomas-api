package com.jordi9.doscomas.feature.planning.outbound.account

import com.jordi9.doscomas.feature.planning.domain.Currency

object CurrencyMapper {
  fun toDatabase(currency: Currency): String = when (currency) {
    Currency.EUR -> "EUR"
  }

  fun toDomain(value: String): Currency = when (value) {
    "EUR" -> Currency.EUR
    else -> error("Unknown currency in database: $value")
  }
}
