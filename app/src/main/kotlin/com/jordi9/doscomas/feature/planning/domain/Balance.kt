package com.jordi9.doscomas.feature.planning.domain

import com.jordi9.doscomas.shared.domain.validate

@JvmInline
value class Balance(val cents: Long) {
  init {
    validate(cents >= 0) { "Balance must be non-negative" }
  }
}
