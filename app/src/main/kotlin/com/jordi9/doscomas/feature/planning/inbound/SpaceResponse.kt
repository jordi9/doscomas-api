package com.jordi9.doscomas.feature.planning.inbound

import com.jordi9.doscomas.feature.planning.domain.Space
import kotlinx.serialization.Serializable

@Serializable
data class SpaceResponse(
  val id: String,
  val name: String,
  val createdAt: String,
  val updatedAt: String
)

fun Space.toResponse() = SpaceResponse(
  id = id.value,
  name = name,
  createdAt = createdAt.toString(),
  updatedAt = updatedAt.toString()
)
