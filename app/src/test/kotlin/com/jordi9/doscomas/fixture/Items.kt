package com.jordi9.doscomas.fixture

import com.jordi9.doscomas.feature.item.domain.Item
import com.jordi9.doscomas.feature.item.domain.ItemId
import java.time.Instant

object Items {
  fun one(
    id: ItemId = ItemId(1),
    name: String = "Test Item",
    description: String? = null,
    createdAt: Instant = Instant.parse("2024-01-01T00:00:00Z"),
    updatedAt: Instant = Instant.parse("2024-01-01T00:00:00Z")
  ) = Item(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt
  )

  fun inserted(name: String = "Test Item", description: String? = null): ItemRow = ItemTable.insert(
    name = name,
    description = description
  )
}
