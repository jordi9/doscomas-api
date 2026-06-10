package com.jordi9.doscomas.feature.planning.inbound.account

import com.jordi9.doscomas.feature.planning.domain.Balance
import com.jordi9.doscomas.feature.planning.domain.MonthlyContribution
import com.jordi9.doscomas.feature.planning.inbound.badRequest
import com.jordi9.doscomas.feature.planning.inbound.validateRequest
import kotlin.math.absoluteValue

internal fun toBalance(value: String): Balance = Balance(toCents(value))

internal fun toMonthlyContribution(value: String): MonthlyContribution = MonthlyContribution(toCents(value))

internal fun Balance.toResponse(): String = centsToResponse(cents)

internal fun MonthlyContribution.toResponse(): String = centsToResponse(cents)

private fun toCents(value: String): Long {
  validateRequest(AMOUNT_FORMAT.matches(value)) { "Invalid amount format" }

  val amount = value.toBigDecimal()
  validateRequest(amount.scale() <= AMOUNT_SCALE) { "Invalid amount precision" }

  return try {
    amount.movePointRight(AMOUNT_SCALE).longValueExact()
  } catch (_: ArithmeticException) {
    badRequest("Invalid amount format")
  }
}

private fun centsToResponse(cents: Long): String {
  val sign = if (cents < 0) "-" else ""
  val absolute = cents.absoluteValue
  val euros = absolute / 100
  val centsPart = (absolute % 100).toString().padStart(2, '0')
  return "$sign$euros.$centsPart"
}

private const val AMOUNT_SCALE = 2
private val AMOUNT_FORMAT = Regex("""-?\d+(?:\.\d+)?""")
