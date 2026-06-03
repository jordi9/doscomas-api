package com.jordi9.doscomas

import com.jordi9.doscomas.fixture.AccountTable
import com.jordi9.doscomas.fixture.ItemTable
import com.jordi9.doscomas.fixture.SpaceTable
import com.jordi9.kogiven.required
import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import java.io.File

fun jdbi() = InMemoryDB.jdbi

object InMemoryDB {
  var dbUrl: String by required()
  var jdbi: Jdbi by required()
}

object InMemoryDBExtension : TempDirectory(
  prefix = "test_db_",
  before = { dir ->
    val dbFile = File(dir, "test.db")
    InMemoryDB.dbUrl = "jdbc:sqlite:${dbFile.absolutePath}"
    InMemoryDB.jdbi = Jdbi.create(InMemoryDB.dbUrl).installPlugin(KotlinPlugin())
  }
)

object DeleteTablesExtension : AfterEachListener {
  override suspend fun afterEach(testCase: TestCase, result: TestResult) {
    AccountTable.deleteAll()
    SpaceTable.deleteAll()
    ItemTable.deleteAll()
  }
}
