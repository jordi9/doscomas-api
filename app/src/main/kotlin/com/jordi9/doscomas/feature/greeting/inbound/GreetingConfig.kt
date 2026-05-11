package com.jordi9.doscomas.feature.greeting.inbound

import kotlinx.serialization.Serializable

@Serializable
data class GreetingConfig(
  val message: String
)
