package com.echernikova.editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

private const val NOTIFICATION_DELETE_DELAY = 8000L

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
            delay(NOTIFICATION_DELETE_DELAY)

            if (lastUpdated == timestamp) {
                SwingUtilities.invokeLater {
                    callback.invoke("", Status.INFO)
                }
            }
        }

        if (type == Status.ERROR) {
            JOptionPane.showMessageDialog(
                /* parentComponent = */ null,
                /* message = */ status,
                /* title = */ "Cannot save file",
                /* messageType = */ JOptionPane.ERROR_MESSAGE
            )
        }
    }
}

enum class Status {
    ERROR,
    INFO
}
