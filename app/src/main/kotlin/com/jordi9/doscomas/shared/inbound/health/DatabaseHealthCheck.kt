package com.jordi9.doscomas.shared.inbound.health

import com.jordi9.doscomas.Registry
import com.jordi9.krat.jdbi.handle
import com.jordi9.krat.pack.core.HealthCheck
import com.jordi9.krat.pack.core.HealthCheckResult
import kotlinx.coroutines.withTimeout
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import kotlin.time.Duration.Companion.seconds

class DatabaseHealthCheck(
  private val jdbi: Jdbi,
  private val timeoutSeconds: Int = 5
) : HealthCheck {
  override val name = "database"

  override suspend fun check(): HealthCheckResult = withTimeout(timeoutSeconds.seconds) {
    jdbi.handle<Int> {
      createQuery("SELECT 1")
        .mapTo<Int>()
        .one()
    }
    HealthCheckResult.healthy("Database connection OK")
  }
}

fun DatabaseHealthCheck(registry: Registry) = DatabaseHealthCheck(jdbi = registry.jdbi)
