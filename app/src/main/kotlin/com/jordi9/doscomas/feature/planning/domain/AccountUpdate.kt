package com.jordi9.doscomas.feature.planning.domain

sealed interface AccountUpdate

sealed interface AccountCoreUpdate : AccountUpdate

data class AccountNameUpdate(val value: String) : AccountCoreUpdate

data class AccountCategoryUpdate(val value: AccountCategory) : AccountCoreUpdate

data class AccountBalanceUpdate(val value: Money) : AccountCoreUpdate

data class AccountMonthlyContributionUpdate(val value: Money) : AccountCoreUpdate

data class AccountCurrencyUpdate(val value: String) : AccountCoreUpdate

data class AccountNoteUpdate(val value: String?) : AccountCoreUpdate

data class AccountDisplayUpdate(val value: AccountDisplay) : AccountUpdate
