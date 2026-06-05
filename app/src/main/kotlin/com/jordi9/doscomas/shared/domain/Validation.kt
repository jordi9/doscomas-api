package com.jordi9.doscomas.shared.domain

fun validate(condition: Boolean, lazyMessage: () -> String) {
  if (!condition) {
    throw InvalidDataException(lazyMessage())
  }
}
