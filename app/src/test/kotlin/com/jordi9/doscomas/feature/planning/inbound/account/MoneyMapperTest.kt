package com.jordi9.doscomas.feature.planning.inbound.account

import com.jordi9.doscomas.feature.planning.domain.Money
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.plugins.BadRequestException

class MoneyMapperTest : StringSpec({

  "map request money strings to cents" {
    mapOf(
      "0" to 0L,
      "0.00" to 0L,
      "1" to 100L,
      "1.2" to 120L,
      "1.23" to 123L,
      "-353.00" to -35_300L
    ).forEach { (value, cents) ->
      toMoney(value) shouldBe Money(cents)
    }
  }

  "format money responses with exactly two decimals" {
    Money(0).toResponse() shouldBe "0.00"
    Money(5).toResponse() shouldBe "0.05"
    Money(100).toResponse() shouldBe "1.00"
    Money(-35_300).toResponse() shouldBe "-353.00"
  }

  "reject invalid money format" {
    listOf("", "-", "1.", "one", "+1.00", "1e3", "1.2.3").forEach { value ->
      moneyError(value) shouldBe "Invalid money format"
    }
  }

  "reject invalid money precision" {
    listOf("1.001", "-1.234").forEach { value ->
      moneyError(value) shouldBe "Invalid money precision"
    }
  }

  "reject money outside the cents range" {
    moneyError("92233720368547758.08") shouldBe "Invalid money format"
  }
})

private fun moneyError(value: String): String = checkNotNull(
  shouldThrow<BadRequestException> { toMoney(value) }
    .message
)
