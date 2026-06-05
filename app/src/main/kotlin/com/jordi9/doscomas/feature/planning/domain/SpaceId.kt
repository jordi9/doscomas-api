package com.jordi9.doscomas.feature.planning.domain

import com.jordi9.doscomas.shared.domain.NanoId

@JvmInline
value class SpaceId(
  val value: String
) {
  init {
    validate(NanoId.isValid(PREFIX, value)) { "Invalid space ID format" }
  }

  companion object {
    const val PREFIX = "sp_"
  }
}
