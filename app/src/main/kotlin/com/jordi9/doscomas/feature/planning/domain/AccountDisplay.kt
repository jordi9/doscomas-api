package com.jordi9.doscomas.feature.planning.domain

data class AccountDisplay(
  val initials: String? = null,
  val color: String? = null,
  val typeLabel: String? = null,
  val subtitle: String? = null
) {
  fun isEmpty(): Boolean = initials == null && color == null && typeLabel == null && subtitle == null
}
