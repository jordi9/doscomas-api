package com.jordi9.doscomas.feature.planning.domain

private val colorPattern = Regex("^#[0-9A-Fa-f]{6}$")

class ValidationException(
  message: String
) : Exception(message)

fun validate(condition: Boolean, lazyMessage: () -> String) {
  if (!condition) {
    throw ValidationException(lazyMessage())
  }
}

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

fun validDisplayText(field: String, value: String): String {
  validate(value.length <= 500) { "$field is too long" }
  return value
}

fun validInitials(value: String): String {
  validate(value.length <= 8) { "Initials are too long" }
  return value
}

fun validColor(value: String): String {
  validate(colorPattern.matches(value)) { "Invalid display color" }
  return value
}

fun validDisplay(value: AccountDisplay): AccountDisplay = AccountDisplay(
  initials = value.initials?.let(::validInitials),
  color = value.color?.let(::validColor),
  typeLabel = value.typeLabel?.let { validDisplayText("Type label", it) },
  subtitle = value.subtitle?.let { validDisplayText("Subtitle", it) }
)

fun validCurrency(value: String): String {
  validate(value == "EUR") { "Only EUR currency is supported" }
  return value
}

fun validBalance(value: Money): Money {
  validate(value.cents >= 0) { "Balance must be non-negative" }
  return value
}
