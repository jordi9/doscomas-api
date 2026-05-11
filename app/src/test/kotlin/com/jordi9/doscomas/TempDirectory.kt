package com.jordi9.doscomas

import io.kotest.core.listeners.ProjectListener
import java.io.File
import java.nio.file.Files

abstract class TempDirectory(
  private val prefix: String,
  private val before: (File) -> Unit = {},
  private val after: (File) -> Unit = {}
) : ProjectListener {
  lateinit var tempDir: File
    private set

  override suspend fun beforeProject() {
    tempDir = Files.createTempDirectory(prefix).toFile()
    before(tempDir)
  }

  override suspend fun afterProject() {
    if (this::tempDir.isInitialized) {
      runCatching {
        after(tempDir)
        tempDir.deleteRecursively()
      }.onFailure { cause ->
        throw IllegalStateException("Temp directory '$tempDir' could not be deleted", cause)
      }
    }
  }
}
