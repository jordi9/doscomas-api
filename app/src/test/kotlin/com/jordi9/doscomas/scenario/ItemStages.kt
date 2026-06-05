package com.jordi9.doscomas.scenario

import com.jordi9.doscomas.NotificationStub
import com.jordi9.doscomas.feature.item.domain.ItemId
import com.jordi9.doscomas.fixture.ItemExample
import com.jordi9.doscomas.fixture.ItemTable
import com.jordi9.doscomas.httpClient
import com.jordi9.kogiven.StageContext
import com.jordi9.kogiven.required
import com.jordi9.krat.pack.test.JsonResponse
import com.jordi9.krat.pack.test.jsonBody
import com.jordi9.krat.pack.test.toJsonResponse
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode

class ItemContext {
  var status: HttpStatusCode by required()
  var response: JsonResponse by required()
  val insertedItemIds: MutableList<ItemId> = mutableListOf()
}

class GivenItem : StageContext<GivenItem, ItemContext>() {
  fun `no items exist`() = apply {
    // Database is empty by default
  }

  fun `an item exists`(name: String, description: String? = null) = apply {
    val item = ItemExample(name = name, description = description)
    val row = ItemTable.insert(item)
    ctx.insertedItemIds.add(row.id)
  }
}

class WhenItem : StageContext<WhenItem, ItemContext>() {
  suspend fun `listing all items`() = apply {
    val response = httpClient().get("/api/v1/items")
    ctx.status = response.status
    if (response.status == HttpStatusCode.OK) {
      ctx.response = response.toJsonResponse()
    }
  }

  suspend fun `creating an item`(name: String, description: String? = null) = apply {
    val body =
      if (description != null) {
        """{"name": "$name", "description": "$description"}"""
      } else {
        """{"name": "$name"}"""
      }
    val response =
      httpClient().post("/api/v1/items") {
        jsonBody(body)
      }
    ctx.status = response.status
    if (response.status == HttpStatusCode.Created) {
      ctx.response = response.toJsonResponse()
    }
  }

  suspend fun `getting item by id`() = apply {
    val response = httpClient().get("/api/v1/items/${ctx.insertedItemIds.last().value}")
    ctx.status = response.status
    if (response.status == HttpStatusCode.OK) {
      ctx.response = response.toJsonResponse()
    }
  }

  suspend fun `getting item with invalid id`() = apply {
    val response = httpClient().get("/api/v1/items/999")
    ctx.status = response.status
  }
}

class ThenItem : StageContext<ThenItem, ItemContext>() {
  fun `the response is successful`() = apply {
    ctx.status shouldBe HttpStatusCode.OK
  }

  fun `the item was created`() = apply {
    ctx.status shouldBe HttpStatusCode.Created
  }

  fun `the response is not found`() = apply {
    ctx.status shouldBe HttpStatusCode.NotFound
  }

  fun `no items are returned`() = apply {
    ctx.response.isEmpty() shouldBe true
  }

  fun `items are returned`(count: Int) = apply {
    ctx.response.items().size shouldBe count
  }

  fun `the item has name`(expected: String) = apply {
    ctx.response.string("name") shouldBe expected
  }

  fun `the item has description`(expected: String) = apply {
    ctx.response.string("description") shouldBe expected
  }

  fun `the item has no description`() = apply {
    ctx.response.stringOrNull("description") shouldBe null
  }

  fun `a notification was sent`(expected: String) = apply {
    NotificationStub.notifications shouldContain expected
  }
}
