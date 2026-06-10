package com.jordi9.doscomas.feature.greeting.inbound

import com.jordi9.doscomas.feature.greeting.application.GetGreetingUseCase
import com.jordi9.doscomas.feature.greeting.application.GreetingConfig
import com.jordi9.krat.pack.core.Handler
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondText

class HelloHandler(
  private val getGreeting: GetGreetingUseCase
) : Handler {
  override suspend fun handle(call: ApplicationCall) {
    val name = call.request.queryParameters["name"]
    call.respondText(getGreeting(name))
  }
}

fun HelloHandler(greetingConfig: GreetingConfig) = HelloHandler(
  getGreeting = GetGreetingUseCase(greetingConfig)
)
