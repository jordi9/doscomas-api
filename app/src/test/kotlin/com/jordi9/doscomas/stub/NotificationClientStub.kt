package com.jordi9.doscomas.stub

import com.jordi9.doscomas.feature.item.domain.NotificationClient

class NotificationClientStub : NotificationClient {
  val notifications = mutableListOf<String>()

  override fun notify(message: String) {
    notifications.add(message)
  }

  fun reset() {
    notifications.clear()
  }
}
