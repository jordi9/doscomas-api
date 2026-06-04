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
import com.jordi9.kogiven.StageContext
import com.jordi9.kogiven.required
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val CURRENT_TIME = "2006-01-02T15:04:05Z"

class PlanningContext {
  var status: HttpStatusCode by required()
  var body: String = ""
  var json: JsonElement? = null
  var currentSpaceId: SpaceId by required()
  var firstSpaceId: SpaceId by required()
  var secondSpaceId: SpaceId by required()
  var currentAccountId: AccountId by required()
}

class GivenPlanning : StageContext<GivenPlanning, PlanningContext>() {
  fun `no spaces exist`() = apply { }

  fun `a space exists`() = apply {
    val row = SpaceTable.insert(SpaceExample())
    ctx.currentSpaceId = row.id
  }

  fun `two spaces exist`() = apply {
    val first = SpaceTable.insert(SpaceExample(name = "First Space"))
    val second = SpaceTable.insert(SpaceExample(name = "Second Space"))
    ctx.firstSpaceId = first.id
    ctx.secondSpaceId = second.id
    ctx.currentSpaceId = second.id
  }

  fun `an account exists in the current space`(name: String, note: String? = null) = apply {
    val account = AccountExample(
      spaceId = ctx.currentSpaceId,
      name = name,
      note = note
    )
    val row = AccountTable.insert(account)
    ctx.currentAccountId = row.id
  }

  fun `an account exists in the second space`() = apply {
    val account = AccountExample(
      spaceId = ctx.secondSpaceId,
      name = "Other Cash"
    )
    val row = AccountTable.insert(account)
    ctx.currentAccountId = row.id
  }

  fun `an account exists with display in the current space`() = apply {
    val account = AccountExample(
      spaceId = ctx.currentSpaceId,
      name = "Cash",
      display = AccountDisplay(
        initials = "CA",
        color = "#112233",
        typeLabel = "Cash",
        subtitle = "Main account"
      )
    )
    val row = AccountTable.insert(account)
    ctx.currentAccountId = row.id
  }
}

class WhenPlanning : StageContext<WhenPlanning, PlanningContext>() {
  suspend fun `listing spaces`() = apply {
    ctx.capture(httpClient().get("/api/v1/spaces"))
  }

  suspend fun `creating a space`() = apply {
    ctx.capture(
      httpClient().post("/api/v1/spaces") {
        contentType(ContentType.Application.Json)
        setBody("""{"name":"FIRE"}""")
      }
    )
    if (ctx.status == HttpStatusCode.Created) {
      ctx.currentSpaceId = SpaceId(ctx.obj().string("id"))
    }
  }

  suspend fun `getting the current space`() = apply {
    ctx.capture(httpClient().get("/api/v1/spaces/${ctx.currentSpaceId.value}"))
  }

  suspend fun `creating an account in the current space`() = apply {
    ctx.capture(
      httpClient().post("/api/v1/spaces/${ctx.currentSpaceId.value}/accounts") {
        contentType(ContentType.Application.Json)
        setBody(
          """
            {
              "name": "Cash",
              "category": "cash",
              "balance": "8420",
              "note": "Main cash account"
            }
          """.trimIndent()
        )
      }
    )
    if (ctx.status == HttpStatusCode.Created) {
      ctx.currentAccountId = AccountId(ctx.obj().string("id"))
    }
  }

  suspend fun `listing accounts in the current space`() = apply {
    ctx.capture(httpClient().get("/api/v1/spaces/${ctx.currentSpaceId.value}/accounts"))
  }

  suspend fun `getting the current account`() = apply {
    ctx.capture(httpClient().get(ctx.currentAccountPath()))
  }

  suspend fun `getting the other space account from the first space`() = apply {
    ctx.capture(
      httpClient().get("/api/v1/spaces/${ctx.firstSpaceId.value}/accounts/${ctx.currentAccountId.value}")
    )
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
    )
  }

  suspend fun `patching account balance`() = apply {
    ctx.patchCurrentAccount("""{"balance":"200.50"}""")
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
    )
  }

  suspend fun `clearing account display`() = apply {
    ctx.patchCurrentAccount(
      """
        {
          "display": {
            "initials": null,
            "color": null,
            "typeLabel": null,
            "subtitle": null
          }
        }
      """.trimIndent()
    )
  }

  suspend fun `clearing account note`() = apply {
    ctx.patchCurrentAccount("""{"note":null}""")
  }

  suspend fun `creating an account with invalid money precision`() = apply {
    ctx.capture(ctx.postAccount("""{"name":"Cash","category":"cash","balance":"1.001"}"""))
  }

  suspend fun `creating an account with negative balance`() = apply {
    ctx.capture(ctx.postAccount("""{"name":"Cash","category":"cash","balance":"-1.00"}"""))
  }

  suspend fun `creating an account with invalid category`() = apply {
    ctx.capture(ctx.postAccount("""{"name":"Cash","category":"invalid","balance":"1.00"}"""))
  }

  suspend fun `getting a space with invalid id`() = apply {
    ctx.capture(httpClient().get("/api/v1/spaces/not-a-space"))
  }

  suspend fun `creating an account in a missing space`() = apply {
    ctx.capture(
      httpClient().post("/api/v1/spaces/${spaceId().value}/accounts") {
        contentType(ContentType.Application.Json)
        setBody("""{"name":"Cash","category":"cash","balance":"1.00"}""")
      }
    )
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

  fun `no resources are returned`() = apply {
    ctx.array().size shouldBe 0
  }

  fun `one resource is returned`() = apply {
    ctx.array().size shouldBe 1
  }

  fun `the space id is public`() = apply {
    ctx.obj().string("id") shouldStartWith "sp_"
  }

  fun `the account id is public`() = apply {
    ctx.obj().string("id") shouldStartWith "acc_"
  }

  fun `the space has name FIRE`() = apply {
    ctx.obj().string("name") shouldBe "FIRE"
  }

  fun `timestamps are current`() = apply {
    ctx.obj().string("createdAt") shouldBe CURRENT_TIME
    ctx.obj().string("updatedAt") shouldBe CURRENT_TIME
  }

  fun `updated at is current`() = apply {
    ctx.obj().string("updatedAt") shouldBe CURRENT_TIME
  }

  fun `balance updated at is current`() = apply {
    ctx.obj().string("balanceUpdatedAt") shouldBe CURRENT_TIME
  }

  fun `the account belongs to the current space`() = apply {
    ctx.obj().string("spaceId") shouldBe ctx.currentSpaceId.value
  }

  fun `the listed account belongs to the current space`() = apply {
    ctx.array().first().jsonObject.string("spaceId") shouldBe ctx.currentSpaceId.value
  }

  fun `the account has name`(expected: String) = apply {
    ctx.obj().string("name") shouldBe expected
  }

  fun `the account has create defaults`() = apply {
    val account = ctx.obj()
    account.keys shouldBe setOf(
      "id",
      "spaceId",
      "name",
      "category",
      "balance",
      "monthlyContribution",
      "currency",
      "note",
      "balanceUpdatedAt",
      "createdAt",
      "updatedAt",
      "display"
    )
    account.string("name") shouldBe "Cash"
    account.string("category") shouldBe "cash"
    account.string("balance") shouldBe "8420.00"
    account.string("monthlyContribution") shouldBe "0.00"
    account.string("currency") shouldBe "EUR"
    account.stringOrNull("note") shouldBe "Main cash account"
    account.string("balanceUpdatedAt") shouldBe CURRENT_TIME
    account.string("createdAt") shouldBe CURRENT_TIME
    account.string("updatedAt") shouldBe CURRENT_TIME
    account.obj("display").keys.shouldBeEmpty()
  }

  fun `core account fields were patched`() = apply {
    val account = ctx.obj()
    account.string("name") shouldBe "Updated Cash"
    account.string("category") shouldBe "investment"
    account.string("monthlyContribution") shouldBe "-353.00"
    account.string("currency") shouldBe "EUR"
    account.stringOrNull("note") shouldBe "updated note"
  }

  fun `balance was patched`() = apply {
    ctx.obj().string("balance") shouldBe "200.50"
  }

  fun `display fields were patched`() = apply {
    val display = ctx.obj().obj("display")
    display.keys shouldBe setOf("initials", "color", "typeLabel", "subtitle")
    display.string("initials") shouldBe "IN"
    display.string("color") shouldBe "#ABCDEF"
    display.string("typeLabel") shouldBe "Investment"
    display.string("subtitle") shouldBe "Long-term"
  }

  fun `display is empty`() = apply {
    ctx.obj().obj("display").keys.shouldBeEmpty()
  }

  fun `display row was deleted`() = apply {
    AccountTable.displayExists(ctx.currentAccountId) shouldBe false
  }

  fun `note is null`() = apply {
    ctx.obj().stringOrNull("note") shouldBe null
  }
}

private suspend fun PlanningContext.capture(response: HttpResponse) {
  status = response.status
  body = response.bodyAsText()
  json = if (body.isBlank()) null else Json.parseToJsonElement(body)
}

private suspend fun PlanningContext.patchCurrentAccount(body: String) {
  capture(
    httpClient().patch(currentAccountPath()) {
      contentType(ContentType.Application.Json)
      setBody(body)
    }
  )
}

private suspend fun PlanningContext.postAccount(body: String): HttpResponse =
  httpClient().post("/api/v1/spaces/${currentSpaceId.value}/accounts") {
    contentType(ContentType.Application.Json)
    setBody(body)
  }

private fun PlanningContext.currentAccountPath(): String =
  "/api/v1/spaces/${currentSpaceId.value}/accounts/${currentAccountId.value}"

private fun PlanningContext.obj(): JsonObject = json!!.jsonObject

private fun PlanningContext.array() = json!!.jsonArray

private fun JsonObject.string(field: String): String = getValue(field).jsonPrimitive.content

private fun JsonObject.stringOrNull(field: String): String? {
  val value = this[field] ?: return null
  if (value is JsonNull) return null
  return value.jsonPrimitive.content
}

private fun JsonObject.obj(field: String): JsonObject = getValue(field).jsonObject
