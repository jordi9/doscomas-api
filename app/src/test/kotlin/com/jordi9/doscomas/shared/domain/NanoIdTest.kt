package com.jordi9.doscomas.shared.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import kotlin.random.Random

class NanoIdTest : StringSpec({

  "generate URL-friendly IDs with the requested prefix and default NanoID size" {
    val id = NanoIds(Random(42)).get("sp_")

    id shouldMatch Regex("^sp_[A-Za-z0-9_-]{21}$")
  }

  "generate unique IDs across a sample" {
    val generator = NanoIds(Random(42))
    val ids = (1..1_000).map { generator.get("acc_") }

    ids.toSet().size shouldBe ids.size
  }

  "validate IDs by prefix, default NanoID size, and URL-friendly suffix" {
    val validSuffix = "a".repeat(21)

    NanoIds.isValid("sp_", "sp_$validSuffix") shouldBe true
    NanoIds.isValid("sp_", "acc_$validSuffix") shouldBe false
    NanoIds.isValid("sp_", "sp_${"a".repeat(20)}") shouldBe false
    NanoIds.isValid("sp_", "sp_${"a".repeat(22)}") shouldBe false
    NanoIds.isValid("sp_", "sp_${"a".repeat(20)}!") shouldBe false
  }

  "validate every URL-friendly alphabet character" {
    val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-"

    alphabet.forEach { char ->
      NanoIds.isValid("sp_", "sp_${char.toString().repeat(21)}") shouldBe true
    }

    "+/=:!".forEach { char ->
      NanoIds.isValid("sp_", "sp_${char.toString().repeat(21)}") shouldBe false
    }
  }
})
