package com.jordi9.doscomas.feature.planning.domain

import com.jordi9.krat.gag.YOLO
import kotlinx.serialization.json.JsonObject

@JvmInline
value class AccountDisplay(
  val value: @YOLO JsonObject
)
