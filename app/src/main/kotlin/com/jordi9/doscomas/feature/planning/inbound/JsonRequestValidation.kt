package com.jordi9.doscomas.feature.planning.inbound

import io.ktor.server.plugins.BadRequestException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

fun badRequest(message: String): Nothing = throw BadRequestException(message)

fun validateRequest(condition: Boolean, lazyMessage: () -> String) {
  if (!condition) {
    badRequest(lazyMessage())
  }
}

fun JsonObject.rejectUnknownFields(knownFields: Set<String>, owner: String) {
  val found = keys.firstOrNull { it !in knownFields }
  validateRequest(found == null) { "Unknown $owner field: $found" }
}

fun JsonElement.stringValue(field: String): String {
  val primitive = this as? JsonPrimitive
  validateRequest(primitive != null && primitive.isString) { "$field must be a string" }
  return primitive?.contentOrNull ?: badRequest("$field must be a string")
}
