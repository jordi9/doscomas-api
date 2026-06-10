package com.jordi9.doscomas.feature.planning.inbound.account

import com.jordi9.doscomas.feature.planning.domain.Balance
import com.jordi9.doscomas.feature.planning.domain.MonthlyContribution
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.plugins.BadRequestException

class AccountAmountMapperTest : StringSpec({

  "map request amount strings to cents" {
    mapOf(
      "0" to 0L,
      "0.00" to 0L,
      "1" to 100L,
      "1.2" to 120L,
      "1.23" to 123L
    ).forEach { (value, cents) ->
      toBalance(value) shouldBe Balance(cents)
    }

    toMonthlyContribution("-353.00") shouldBe MonthlyContribution(-35_300L)
  }

  "format account amounts with exactly two decimals" {
    Balance(0).toResponse() shouldBe "0.00"
    Balance(5).toResponse() shouldBe "0.05"
    Balance(100).toResponse() shouldBe "1.00"
    MonthlyContribution(-35_300).toResponse() shouldBe "-353.00"
  }

  "reject invalid amount format" {
    listOf("", "-", "1.", "one", "+1.00", "1e3", "1.2.3").forEach { value ->
      amountError(value) shouldBe "Invalid amount format"
    }
  }

  "reject invalid amount precision" {
    listOf("1.001", "-1.234").forEach { value ->
      amountError(value) shouldBe "Invalid amount precision"
    }
  }

  "reject amounts outside the cents range" {
    amountError("92233720368547758.08") shouldBe "Invalid amount format"
  }
})

private fun amountError(value: String): String = checkNotNull(
  shouldThrow<BadRequestException> { toMonthlyContribution(value) }
    .message
)
