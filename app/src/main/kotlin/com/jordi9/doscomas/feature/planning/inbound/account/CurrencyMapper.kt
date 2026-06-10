package com.jordi9.doscomas.feature.planning.inbound.account

import com.jordi9.doscomas.feature.planning.domain.Currency
import com.jordi9.doscomas.feature.planning.inbound.badRequest

internal fun toCurrency(value: String): Currency = when (value) {
  "EUR" -> Currency.EUR
  else -> badRequest("Only EUR currency is supported")
}

internal fun Currency.toResponse(): String = when (this) {
  Currency.EUR -> "EUR"
}
