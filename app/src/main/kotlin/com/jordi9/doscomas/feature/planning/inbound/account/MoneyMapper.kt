package com.jordi9.doscomas.feature.planning.inbound.account

import com.jordi9.doscomas.feature.planning.domain.Money
import com.jordi9.doscomas.feature.planning.inbound.badRequest
import com.jordi9.doscomas.feature.planning.inbound.validateRequest
import kotlin.math.absoluteValue

private const val MONEY_SCALE = 2
private val MONEY_FORMAT = Regex("""-?\d+(?:\.\d+)?""")

internal fun toMoney(value: String): Money {
  validateRequest(MONEY_FORMAT.matches(value)) { "Invalid money format" }

  val amount = value.toBigDecimal()
  validateRequest(amount.scale() <= MONEY_SCALE) { "Invalid money precision" }

  return try {
    Money(amount.movePointRight(MONEY_SCALE).longValueExact())
  } catch (e: ArithmeticException) {
    badRequest("Invalid money format")
  }
}

internal fun Money.toResponse(): String {
  val sign = if (cents < 0) "-" else ""
  val absolute = cents.absoluteValue
  val euros = absolute / 100
  val centsPart = (absolute % 100).toString().padStart(2, '0')
  return "$sign$euros.$centsPart"
}
