package com.jordi9.doscomas.feature.planning.domain

import com.jordi9.doscomas.shared.domain.validate

fun validNote(value: String): String {
  validate(value.length <= 500) { "Note is too long" }
  return value
}
