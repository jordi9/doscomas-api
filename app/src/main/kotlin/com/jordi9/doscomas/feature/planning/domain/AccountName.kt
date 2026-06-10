package com.jordi9.doscomas.feature.planning.domain

import com.jordi9.doscomas.shared.domain.validate

@JvmInline
value class AccountName(val value: String) {
  init {
    validate(value.isNotBlank()) { "Name must not be blank" }
    validate(value.length <= 120) { "Name is too long" }
    validate(value == value.trim()) { "Name must be normalized" }
  }
}
