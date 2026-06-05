package com.jordi9.doscomas.feature.planning.domain

sealed interface AccountUpdate

data class AccountNameUpdate(val value: String) : AccountUpdate

data class AccountCategoryUpdate(val value: AccountCategory) : AccountUpdate

data class AccountBalanceUpdate(val value: Money) : AccountUpdate

data class AccountMonthlyContributionUpdate(val value: Money) : AccountUpdate

data class AccountCurrencyUpdate(val value: String) : AccountUpdate

data class AccountNoteUpdate(val value: String?) : AccountUpdate

data class AccountDisplayUpdate(val value: AccountDisplay) : AccountUpdate
