package com.jordi9.doscomas.feature.greeting.application

import com.jordi9.doscomas.feature.greeting.inbound.GreetingConfig

class GetGreetingUseCase(
  private val config: GreetingConfig
) {
  operator fun invoke(name: String?): String = "Hello, ${name ?: config.message}!"
}
