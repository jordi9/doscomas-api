package com.jordi9.doscomas.feature.planning.inbound.account

import com.jordi9.doscomas.feature.planning.domain.AccountDisplay
import com.jordi9.doscomas.feature.planning.inbound.badRequest
import com.jordi9.doscomas.feature.planning.inbound.validateRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

internal fun JsonElement?.toAccountDisplay(): AccountDisplay {
  if (this == null || this is JsonNull) return AccountDisplay(JsonObject(emptyMap()))

  val display = this as? JsonObject ?: badRequest("display must be an object")
  val document = Json.encodeToString(display)
  validateRequest(document.toByteArray(Charsets.UTF_8).size <= ACCOUNT_DISPLAY_MAX_BYTES) { "display is too large" }
  return AccountDisplay(display)
}

private const val ACCOUNT_DISPLAY_MAX_BYTES = 64 * 1024
