package com.jordi9.doscomas.shared.domain

import java.security.SecureRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

class NanoIds(
  private val random: Random = SecureRandom().asKotlinRandom()
) {

  fun get(prefix: String): String = prefix + buildString {
    repeat(DEFAULT_SIZE) {
      append(ALPHABET[random.nextInt(ALPHABET.length)])
    }
  }

  companion object {
    fun isValid(prefix: String, value: String): Boolean {
      val suffix = value.removePrefix(prefix)
      return value.startsWith(prefix) &&
        suffix.length == DEFAULT_SIZE &&
        suffix.all { it in ALPHABET }
    }
  }
}

private const val DEFAULT_SIZE = 21
private const val ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-"
