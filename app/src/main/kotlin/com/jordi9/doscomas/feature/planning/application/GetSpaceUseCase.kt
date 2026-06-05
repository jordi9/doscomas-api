package com.jordi9.doscomas.feature.planning.application

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Space
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.feature.planning.outbound.SpaceRepository
import com.jordi9.doscomas.shared.domain.NotFoundException

class GetSpaceUseCase(
  private val spaces: SpaceRepository
) {
  suspend operator fun invoke(id: SpaceId): Space =
    spaces.findById(id) ?: throw NotFoundException("Space not found: ${id.value}")
}

fun GetSpaceUseCase(registry: Registry) = GetSpaceUseCase(
  spaces = SpaceRepository(registry)
)
