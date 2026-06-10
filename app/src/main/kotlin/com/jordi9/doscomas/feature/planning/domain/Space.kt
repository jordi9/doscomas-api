package com.jordi9.doscomas.feature.planning.domain

import java.time.Instant

data class Space(
  val id: SpaceId,
  val name: SpaceName,
  val createdAt: Instant,
  val updatedAt: Instant
)
