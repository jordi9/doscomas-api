package com.jordi9.doscomas.feature.planning.domain

import com.jordi9.doscomas.shared.domain.NanoIds

@JvmInline
value class AccountId(
  val value: String
) {
  init {
    validate(NanoIds.isValid(PREFIX, value)) { "Invalid account ID format" }
  }

  companion object {
    const val PREFIX = "acc_"
  }
}
