package com.jordi9.doscomas.feature.planning.inbound

import com.jordi9.doscomas.feature.planning.domain.FieldUpdate
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

fun JsonObject.stringUpdate(field: String): FieldUpdate<String> = stringUpdate(field) { it }

fun <T> JsonObject.stringUpdate(field: String, parse: (String) -> T): FieldUpdate<T> =
  update(field) { element -> parse(element.stringValue(field)) }

fun JsonObject.nullableStringUpdate(field: String): FieldUpdate<String?> =
  nullableUpdate(field) { element -> element.stringValue(field) }

fun <T> JsonObject.update(field: String, parse: (JsonElement) -> T): FieldUpdate<T> {
  val element = this[field] ?: return FieldUpdate.Keep
  validateRequest(element !is JsonNull) { "$field cannot be null" }
  return FieldUpdate.Replace(parse(element))
}

fun <T : Any> JsonObject.nullableUpdate(field: String, parse: (JsonElement) -> T): FieldUpdate<T?> {
  val element = this[field] ?: return FieldUpdate.Keep
  if (element is JsonNull) return FieldUpdate.Replace(null)
  return FieldUpdate.Replace(parse(element))
}
