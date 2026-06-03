package com.jordi9.doscomas.feature.planning.application

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Space
import com.jordi9.doscomas.feature.planning.outbound.SpaceRepository

class ListSpacesUseCase(
  private val spaces: SpaceRepository
) {
  suspend operator fun invoke(): List<Space> = spaces.findAll()
}

fun ListSpacesUseCase(registry: Registry) = ListSpacesUseCase(
  spaces = SpaceRepository(registry)
)
