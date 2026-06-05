package com.jordi9.doscomas.feature.planning.domain

import kotlin.math.absoluteValue

@JvmInline
value class Money(
  val cents: Long
) {
  fun toApiString(): String {
    val sign = if (cents < 0) "-" else ""
    val absolute = cents.absoluteValue
    val euros = absolute / 100
    val centsPart = (absolute % 100).toString().padStart(2, '0')
    return "$sign$euros.$centsPart"
  }

  companion object {
    fun parse(value: String): Money {
      val negative = value.startsWith("-")
      val unsigned = value.removePrefix("-")
      val parts = unsigned.split('.')
      validate(unsigned.isNotBlank() && parts.size <= 2) { "Invalid money format" }
      validate(parts[0].isNotEmpty() && parts[0].all(Char::isDigit)) { "Invalid money format" }

      val decimals = parts.getOrNull(1)
      validate(decimals == null || (decimals.length in 1..2 && decimals.all(Char::isDigit))) {
        "Invalid money precision"
      }

      val euros = parts[0].toLong()
      val cents = (decimals ?: "").padEnd(2, '0').ifBlank { "00" }.toLong()
      val amount = euros * 100 + cents
      return Money(if (negative) -amount else amount)
    }
  }
}
