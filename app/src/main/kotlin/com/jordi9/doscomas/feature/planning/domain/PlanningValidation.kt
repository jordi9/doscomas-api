package com.jordi9.doscomas.feature.planning.domain

import com.jordi9.doscomas.shared.domain.validate

fun validName(value: String): String {
  val trimmed = value.trim()
  validate(trimmed.isNotBlank()) { "Name must not be blank" }
  validate(trimmed.length <= 120) { "Name is too long" }
  return trimmed
}

fun validNote(value: String): String {
  validate(value.length <= 500) { "Note is too long" }
  return value
}

fun validCurrency(value: String): String {
  validate(value == "EUR") { "Only EUR currency is supported" }
  return value
}

fun validBalance(value: Money): Money {
  validate(value.cents >= 0) { "Balance must be non-negative" }
  return value
}
