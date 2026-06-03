package com.jordi9.doscomas.feature.planning.domain

private val colorPattern = Regex("^#[0-9A-Fa-f]{6}$")

fun validName(value: String): String {
  val trimmed = value.trim()
  require(trimmed.isNotBlank()) { "Name must not be blank" }
  require(trimmed.length <= 120) { "Name is too long" }
  return trimmed
}

fun validNote(value: String): String {
  require(value.length <= 500) { "Note is too long" }
  return value
}

fun validDisplayText(field: String, value: String): String {
  require(value.length <= 500) { "$field is too long" }
  return value
}

fun validInitials(value: String): String {
  require(value.length <= 8) { "Initials are too long" }
  return value
}

fun validColor(value: String): String {
  require(colorPattern.matches(value)) { "Invalid display color" }
  return value
}

fun validDisplay(value: AccountDisplay): AccountDisplay = AccountDisplay(
  initials = value.initials?.let(::validInitials),
  color = value.color?.let(::validColor),
  typeLabel = value.typeLabel?.let { validDisplayText("Type label", it) },
  subtitle = value.subtitle?.let { validDisplayText("Subtitle", it) }
)

fun validCurrency(value: String): String {
  require(value == "EUR") { "Only EUR currency is supported" }
  return value
}

fun validBalance(value: Money): Money {
  require(value.cents >= 0) { "Balance must be non-negative" }
  return value
}
