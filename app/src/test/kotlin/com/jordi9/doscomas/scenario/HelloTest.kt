package com.jordi9.doscomas.scenario

import com.jordi9.doscomas.httpClient
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class HelloTest : StringSpec({

  "GET / should return Hello from config" {
    val response = httpClient().get("/")
    response.status.value shouldBe 200
    response.bodyAsText() shouldBe "Hello, Test!"
  }

  "GET /hello should return Hello, name!" {
    val response = httpClient().get("/hello?name=World")
    response.status.value shouldBe 200
    response.bodyAsText() shouldBe "Hello, World!"
  }
})
