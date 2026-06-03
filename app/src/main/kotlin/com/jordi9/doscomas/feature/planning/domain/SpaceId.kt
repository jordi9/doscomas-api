package com.jordi9.doscomas.feature.planning.domain

import com.jordi9.doscomas.shared.domain.PublicIdGenerator

@JvmInline
value class SpaceId(
  val value: String
) {
  init {
    require(PublicIdGenerator.isValid(PREFIX, value)) { "Invalid space ID format" }
  }

  companion object {
    const val PREFIX = "sp_"
  }
}
