package com.jordi9.doscomas.scenario

import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import com.jordi9.doscomas.feature.planning.domain.AccountId
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.fixture.AccountExample
import com.jordi9.doscomas.fixture.AccountTable
import com.jordi9.doscomas.fixture.SpaceExample
import com.jordi9.doscomas.fixture.SpaceTable
import com.jordi9.doscomas.fixture.spaceId
import com.jordi9.doscomas.httpClient
import com.jordi9.doscomas.sharedClock
import com.jordi9.kogiven.StageContext
import com.jordi9.kogiven.required
import com.jordi9.krat.pack.test.JsonResponse
import com.jordi9.krat.pack.test.jsonBody
import com.jordi9.krat.pack.test.toJsonResponse
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

class PlanningContext {
  var status: HttpStatusCode by required()
  var response: JsonResponse by required()
  var spaceId: SpaceId by required()
  var spaceIds: Pair<SpaceId, SpaceId> by required()
  var accountId: AccountId by required()
}

class GivenPlanning : StageContext<GivenPlanning, PlanningContext>() {
  fun `no spaces exist`() = apply { }

  fun `a space exists`() = apply {
    val row = SpaceTable.insert(SpaceExample())
    ctx.spaceId = row.id
  }

  fun `two spaces exist`() = apply {
    val first = SpaceTable.insert(SpaceExample(name = "First Space"))
    val second = SpaceTable.insert(SpaceExample(name = "Second Space"))
    ctx.spaceIds = (first.id to second.id)
    ctx.spaceId = second.id
  }

  fun `an account exists in the current space`(name: String, note: String? = null) = apply {
    val account = AccountExample(
      spaceId = ctx.spaceId,
      name = name,
      note = note
    )
    val row = AccountTable.insert(account)
    ctx.accountId = row.id
  }

  fun `an account exists in the second space`() = apply {
    val account = AccountExample(
      spaceId = ctx.spaceIds.second,
      name = "Other Cash"
    )
    val row = AccountTable.insert(account)
    ctx.accountId = row.id
  }

  fun `an account exists with display in the current space`() = apply {
    val account = AccountExample(
      spaceId = ctx.spaceId,
      name = "Cash",
      display = AccountDisplay(
        initials = "CA",
        color = "#112233",
        typeLabel = "Cash",
        subtitle = "Main account"
      )
    )
    val row = AccountTable.insert(account)
    ctx.accountId = row.id
  }
}

class WhenPlanning : StageContext<WhenPlanning, PlanningContext>() {
  suspend fun `listing spaces`() = apply {
    httpClient().get("/api/v1/spaces").let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `creating a space`() = apply {
    httpClient().post("/api/v1/spaces") {
      jsonBody("""{"name":"FIRE"}""")
    }.let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
      if (ctx.status == HttpStatusCode.Created) {
        ctx.spaceId = SpaceId(ctx.response.string("id"))
      }
    }
  }

  suspend fun `getting the current space`() = apply {
    httpClient().get("/api/v1/spaces/${ctx.spaceId.value}").let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `creating an account in the current space`() = apply {
    httpClient().post("/api/v1/spaces/${ctx.spaceId.value}/accounts") {
      jsonBody(
        """
          {
            "name": "Cash",
            "category": "cash",
            "balance": "8420",
            "note": "Main cash account"
          }
        """
      )
    }.let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
      if (ctx.status == HttpStatusCode.Created) {
        ctx.accountId = AccountId(ctx.response.string("id"))
      }
    }
  }

  suspend fun `listing accounts in the current space`() = apply {
    httpClient().get("/api/v1/spaces/${ctx.spaceId.value}/accounts").let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `getting the current account by ID`() = apply {
    httpClient().get("/api/v1/accounts/${ctx.accountId.value}").let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `patching core account fields`() = apply {
    ctx.patchCurrentAccount(
      """
        {
          "name": "Updated Cash",
          "category": "investment",
          "monthlyContribution": "-353.00",
          "currency": "EUR",
          "note": "updated note"
        }
      """.trimIndent()
    ).let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `patching account balance`() = apply {
    ctx.patchCurrentAccount("""{"balance":"200.50"}""").let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `patching account display`() = apply {
    ctx.patchCurrentAccount(
      """
        {
          "display": {
            "initials": "IN",
            "color": "#ABCDEF",
            "typeLabel": "Investment",
            "subtitle": "Long-term"
          }
        }
      """.trimIndent()
    ).let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `patching account display color only`() = apply {
    ctx.patchCurrentAccount(
      """
        {
          "display": {
            "color": "#ABCDEF"
          }
        }
      """.trimIndent()
    ).let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `clearing account display`() = apply {
    ctx.patchCurrentAccount("""{"display":null}""").let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `patching account display with unknown field`() = apply {
    ctx.patchCurrentAccount(
      """
        {
          "display": {
            "color": "#ABCDEF",
            "emoji": "💰"
          }
        }
      """.trimIndent()
    ).let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `patching account name to null`() = apply {
    ctx.patchCurrentAccount("""{"name":null}""").let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `clearing account note`() = apply {
    ctx.patchCurrentAccount("""{"note":null}""").let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `creating an account with invalid money precision`() = apply {
    ctx.postAccount("""{"name":"Cash","category":"cash","balance":"1.001"}""").let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `creating an account without balance`() = apply {
    ctx.postAccount("""{"name":"Cash","category":"cash"}""").let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `creating an account with numeric balance`() = apply {
    ctx.postAccount("""{"name":"Cash","category":"cash","balance":1.00}""").let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `creating an account with negative balance`() = apply {
    ctx.postAccount("""{"name":"Cash","category":"cash","balance":"-1.00"}""").let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `creating an account with invalid category`() = apply {
    ctx.postAccount("""{"name":"Cash","category":"invalid","balance":"1.00"}""").let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `getting a space with invalid id`() = apply {
    httpClient().get("/api/v1/spaces/not-a-space").let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }

  suspend fun `creating an account in a missing space`() = apply {
    httpClient().post("/api/v1/spaces/${spaceId().value}/accounts") {
      jsonBody("""{"name":"Cash","category":"cash","balance":"1.00"}""")
    }.let { response ->
      ctx.response = response.toJsonResponse()
      ctx.status = response.status
    }
  }
}

class ThenPlanning : StageContext<ThenPlanning, PlanningContext>() {
  fun `the response is successful`() = apply {
    ctx.status shouldBe HttpStatusCode.OK
  }

  fun `a resource was created`() = apply {
    ctx.status shouldBe HttpStatusCode.Created
  }

  fun `the response is not found`() = apply {
    ctx.status shouldBe HttpStatusCode.NotFound
  }

  fun `the response is bad request`() = apply {
    ctx.status shouldBe HttpStatusCode.BadRequest
  }

  fun `the error is`(expected: String) = apply {
    ctx.response.string("error") shouldBe expected
  }

  fun `the error starts with`(expected: String) = apply {
    ctx.response.string("error") shouldStartWith expected
  }

  fun `no resources are returned`() = apply {
    ctx.response.isEmpty() shouldBe true
  }

  fun `one resource is returned`() = apply {
    ctx.response.size shouldBe 1
  }

  fun `the space id is public`() = apply {
    ctx.response.string("id") shouldStartWith "sp_"
  }

  fun `the account id is public`() = apply {
    ctx.response.string("id") shouldStartWith "acc_"
  }

  fun `the space has name FIRE`() = apply {
    ctx.response.string("name") shouldBe "FIRE"
  }

  fun `timestamps are current`() = apply {
    ctx.response.string("createdAt") shouldBe currentTime()
    ctx.response.string("updatedAt") shouldBe currentTime()
  }

  fun `updated at is current`() = apply {
    ctx.response.string("updatedAt") shouldBe currentTime()
  }

  fun `balance updated at is current`() = apply {
    ctx.response.string("balanceUpdatedAt") shouldBe currentTime()
  }

  fun `the account belongs to the current space`() = apply {
    ctx.response.string("spaceId") shouldBe ctx.spaceId.value
  }

  fun `the listed account belongs to the current space`() = apply {
    ctx.response.items().first().string("spaceId") shouldBe ctx.spaceId.value
  }

  fun `the account has name`(expected: String) = apply {
    ctx.response.string("name") shouldBe expected
  }

  fun `the account has create defaults`() = apply {
    with(ctx.response) {
      string("name") shouldBe "Cash"
      string("category") shouldBe "cash"
      string("balance") shouldBe "8420.00"
      string("monthlyContribution") shouldBe "0.00"
      string("currency") shouldBe "EUR"
      stringOrNull("note") shouldBe "Main cash account"
      string("balanceUpdatedAt") shouldBe currentTime()
      string("createdAt") shouldBe currentTime()
      string("updatedAt") shouldBe currentTime()
      obj("display").keys.shouldBeEmpty()
    }
  }

  fun `core account fields were patched`() = apply {
    with(ctx.response) {
      string("name") shouldBe "Updated Cash"
      string("category") shouldBe "investment"
      string("monthlyContribution") shouldBe "-353.00"
      string("currency") shouldBe "EUR"
      stringOrNull("note") shouldBe "updated note"
    }
  }

  fun `balance was patched`() = apply {
    ctx.response.string("balance") shouldBe "200.50"
  }

  fun `display fields were patched`() = apply {
    val display = ctx.response.obj("display")
    display.string("initials") shouldBe "IN"
    display.string("color") shouldBe "#ABCDEF"
    display.string("typeLabel") shouldBe "Investment"
    display.string("subtitle") shouldBe "Long-term"
  }

  fun `only display color is present`() = apply {
    val display = ctx.response.obj("display")
    display.keys shouldBe setOf("color")
    display.string("color") shouldBe "#ABCDEF"
  }

  fun `display is empty`() = apply {
    ctx.response.obj("display").keys.shouldBeEmpty()
  }

  fun `display row was deleted`() = apply {
    AccountTable.displayExists(ctx.accountId) shouldBe false
  }

  fun `note is null`() = apply {
    ctx.response.stringOrNull("note") shouldBe null
  }

  private fun currentTime(): String = sharedClock().now().toString()
}

private suspend fun PlanningContext.patchCurrentAccount(body: String): HttpResponse =
  httpClient().patch(currentAccountPath()) {
    jsonBody(body)
  }

private suspend fun PlanningContext.postAccount(body: String): HttpResponse =
  httpClient().post("/api/v1/spaces/${spaceId.value}/accounts") {
    jsonBody(body)
  }

private fun PlanningContext.currentAccountPath(): String = "/api/v1/accounts/${accountId.value}"
