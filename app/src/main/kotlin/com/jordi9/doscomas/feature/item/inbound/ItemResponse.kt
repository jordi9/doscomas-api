package com.jordi9.doscomas.feature.item.inbound

import com.jordi9.doscomas.feature.item.domain.Item
import kotlinx.serialization.Serializable

@Serializable
data class ItemResponse(
  val id: Long,
  val name: String,
  val description: String?,
  val createdAt: String,
  val updatedAt: String
)

fun Item.toResponse() = ItemResponse(
  id = id.value,
  name = name,
  description = description,
  createdAt = createdAt.toString(),
  updatedAt = updatedAt.toString()
)
