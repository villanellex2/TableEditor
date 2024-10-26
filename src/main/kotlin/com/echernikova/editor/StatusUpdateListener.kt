package com.echernikova.editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.swing.SwingUtilities

class StatusUpdateListener(
    private val callback: (String, Status) -> Unit,
    private val coroutineScope: CoroutineScope,
) {
    private var lastUpdated: Long = 0

    fun onStatusUpdated(status: String, type: Status) {
        val timestamp = System.currentTimeMillis()
        lastUpdated = timestamp

        callback.invoke(status, type)
        coroutineScope.launch {
            delay(4000)

            if (lastUpdated == timestamp) {
                SwingUtilities.invokeLater {
                    callback.invoke("", Status.INFO)
                }
            }
        }
    }
}

enum class Status {
    ERROR,
    INFO
}
