package com.echernikova.editor

import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class EditorMenuBar(
    editorViewModel: EditorViewModel,
    private val onStatusUpdateListener: StatusUpdateListener,
): JMenuBar() {

    init {
        val fileMenu = JMenu("File")
        add(fileMenu)

        val saveMenuItem = JMenuItem("Save")
        saveMenuItem.addActionListener {
            editorViewModel.onSaveClicked(onStatusUpdateListener)
        }
        fileMenu.add(saveMenuItem)
    }
}
