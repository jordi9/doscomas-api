package com.jordi9.doscomas.feature.planning.domain

import com.jordi9.doscomas.shared.domain.NanoId

@JvmInline
value class AccountId(
  val value: String
) {
  init {
    require(NanoId.isValid(PREFIX, value)) { "Invalid account ID format" }
  }

  companion object {
    const val PREFIX = "acc_"
  }
}
