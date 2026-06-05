package com.jordi9.doscomas.feature.planning.inbound

import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import kotlinx.serialization.Serializable

@Serializable
data class AccountDisplayRequest(
  val initials: String? = null,
  val color: String? = null,
  val typeLabel: String? = null,
  val subtitle: String? = null
)

fun AccountDisplayRequest.toDomain() = AccountDisplay(
  initials = initials,
  color = color,
  typeLabel = typeLabel,
  subtitle = subtitle
)
