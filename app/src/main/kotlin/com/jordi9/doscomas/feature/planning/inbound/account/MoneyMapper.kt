package com.jordi9.doscomas.feature.planning.inbound.account

import com.jordi9.doscomas.feature.planning.domain.Money
import kotlin.math.absoluteValue

internal fun Money.toResponse(): String {
  val sign = if (cents < 0) "-" else ""
  val absolute = cents.absoluteValue
  val euros = absolute / 100
  val centsPart = (absolute % 100).toString().padStart(2, '0')
  return "$sign$euros.$centsPart"
}
