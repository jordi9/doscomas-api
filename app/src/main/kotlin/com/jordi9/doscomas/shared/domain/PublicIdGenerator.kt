package com.jordi9.doscomas.shared.domain

import kotlin.random.Random

class PublicIdGenerator(
  private val seed: Int? = null,
  private val size: Int = DEFAULT_SIZE
) {
  private var random: Random = seed?.let { Random(it) } ?: Random.Default

  fun generate(prefix: String): String = prefix + buildString {
    repeat(size) {
      append(ALPHABET[random.nextInt(ALPHABET.length)])
    }
  }

  fun reset() {
    seed?.let { random = Random(it) }
  }

  companion object {
    private const val DEFAULT_SIZE = 21
    private const val ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-"

    fun isValid(prefix: String, value: String): Boolean {
      val suffix = value.removePrefix(prefix)
      return value.startsWith(prefix) &&
        suffix.length == DEFAULT_SIZE &&
        suffix.all { it in ALPHABET }
    }
  }
}
