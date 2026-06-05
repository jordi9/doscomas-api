package com.jordi9.doscomas.feature.planning.domain

sealed interface FieldUpdate<out T> {
  data object Keep : FieldUpdate<Nothing>

  data class Replace<T>(val value: T) : FieldUpdate<T>
}

fun <T, R> FieldUpdate<T>.map(transform: (T) -> R): FieldUpdate<R> = when (this) {
  FieldUpdate.Keep -> FieldUpdate.Keep
  is FieldUpdate.Replace -> FieldUpdate.Replace(transform(value))
}
