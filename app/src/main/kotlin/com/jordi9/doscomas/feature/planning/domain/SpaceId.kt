package com.jordi9.doscomas.feature.planning.domain

import com.jordi9.doscomas.shared.domain.NanoIds

@JvmInline
value class SpaceId(
  val value: String
) {
  init {
    validate(NanoIds.isValid(PREFIX, value)) { "Invalid space ID format" }
  }

  companion object {
    const val PREFIX = "sp_"
  }
}
