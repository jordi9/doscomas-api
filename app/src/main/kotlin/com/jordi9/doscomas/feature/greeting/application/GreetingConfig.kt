package com.jordi9.doscomas.feature.greeting.application

import kotlinx.serialization.Serializable

@Serializable
data class GreetingConfig(
  val message: String
)
