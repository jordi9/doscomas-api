package com.jordi9.doscomas.feature.planning.application

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.planning.domain.Space
import com.jordi9.doscomas.feature.planning.domain.SpaceId
import com.jordi9.doscomas.feature.planning.domain.validName
import com.jordi9.doscomas.feature.planning.outbound.SpaceRepository
import com.jordi9.doscomas.shared.domain.PublicIdGenerator
import com.jordi9.krat.time.TimeClock

class CreateSpaceUseCase(
  private val spaces: SpaceRepository,
  private val ids: PublicIdGenerator,
  private val clock: TimeClock
) {
  suspend operator fun invoke(name: String): Space {
    val now = clock.now()
    return spaces.save(
      Space(
        id = SpaceId(ids.generate(SpaceId.PREFIX)),
        name = validName(name),
        createdAt = now,
        updatedAt = now
      )
    )
  }
}

fun CreateSpaceUseCase(registry: Registry) = CreateSpaceUseCase(
  spaces = SpaceRepository(registry),
  ids = registry.publicIdGenerator,
  clock = registry.timeClock
)
