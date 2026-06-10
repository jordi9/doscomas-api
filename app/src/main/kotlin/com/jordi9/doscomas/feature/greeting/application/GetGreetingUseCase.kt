package com.jordi9.doscomas.feature.greeting.application

class GetGreetingUseCase(
  private val config: GreetingConfig
) {
  operator fun invoke(name: String?): String = "Hello, ${name ?: config.message}!"
}
