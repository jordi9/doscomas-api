package com.jordi9.doscomas.feature.item.application

import com.jordi9.doscomas.Registry
import com.jordi9.doscomas.feature.item.domain.Item
import com.jordi9.doscomas.feature.item.domain.ItemId
import com.jordi9.doscomas.feature.item.outbound.ItemRepository
import com.jordi9.doscomas.shared.domain.NotFoundException

class GetItemUseCase(
  private val items: ItemRepository
) {
  suspend operator fun invoke(id: ItemId): Item =
    items.findById(id) ?: throw NotFoundException("Item not found: ${id.value}")
}

fun GetItemUseCase(registry: Registry) = GetItemUseCase(
  items = ItemRepository(registry)
)
