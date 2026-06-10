package com.jordi9.doscomas.feature.planning.domain

import com.jordi9.doscomas.shared.domain.InvalidDataException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PlanningValueObjectsTest : StringSpec({

  "accept normalized space and account names" {
    SpaceName("FIRE").value shouldBe "FIRE"
    AccountName("Cash").value shouldBe "Cash"
  }

  "reject blank names" {
    validationMessage { SpaceName(" ") } shouldBe "Name must not be blank"
    validationMessage { AccountName("") } shouldBe "Name must not be blank"
  }

  "reject names longer than 120 characters" {
    val name = "a".repeat(121)

    validationMessage { SpaceName(name) } shouldBe "Name is too long"
    validationMessage { AccountName(name) } shouldBe "Name is too long"
  }

  "reject non-normalized names" {
    validationMessage { SpaceName(" FIRE") } shouldBe "Name must be normalized"
    validationMessage { AccountName("Cash ") } shouldBe "Name must be normalized"
  }

  "balance cannot be negative" {
    Balance(0).cents shouldBe 0
    validationMessage { Balance(-1) } shouldBe "Balance must be non-negative"
  }

  "monthly contribution can be negative" {
    MonthlyContribution(-1).cents shouldBe -1
  }

  "currency supports EUR" {
    Currency.values().toList() shouldBe listOf(Currency.EUR)
  }
})

private fun validationMessage(block: () -> Unit): String = checkNotNull(
  shouldThrow<InvalidDataException> { block() }.message
)
