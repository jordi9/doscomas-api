package com.jordi9.doscomas.shared.outbound.db

import com.jordi9.krat.otel.withSpan
import io.opentelemetry.api.trace.Tracer
import org.flywaydb.core.Flyway

fun runDatabaseMigrations(tracer: Tracer, dbUrl: String) {
  tracer.withSpan("db.migrate") {
    val flyway = Flyway.configure()
      .dataSource(dbUrl, null, null)
      .locations("db/migration")
      .load()

    flyway
      .migrate()
      .also { result ->
        setAttribute("db.migrations.applied", result.migrationsExecuted.toLong())

        result.targetSchemaVersion?.let { setAttribute("db.migrations.target", it) }
      }
  }
}
