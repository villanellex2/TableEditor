package com.echernikova.editor

import com.echernikova.editor.table.TableTheme.statusBarTextSize
import com.echernikova.editor.table.TableView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.border.EmptyBorder

private const val DEFAULT_CELL_MIN_WIDTH = 30
private const val DEFAULT_CELL_WIDTH = 120

class EditorFrame(
    private val frameViewModel: EditorViewModel
) : JFrame() {
    private val table = TableView(frameViewModel.tableViewModel)
    private val scrollPane = JScrollPane(table)
    private val statusText = JLabel()

    init {
        setupFrame()
        setupTable()
        setupStatusText()
        setupSaveShortcut()

        frameViewModel.onStatusUpdateListener = StatusUpdateListener(::onStatusUpdated, CoroutineScope(Dispatchers.Default))
        frameViewModel.tableViewModel.evaluateData()
    }

    private fun setupFrame() {
        title = frameViewModel.editedFileName
        defaultCloseOperation = DISPOSE_ON_CLOSE
        setSize(1200, 800)
        setLocationRelativeTo(null)
        layout = GridBagLayout()

        createMenuBar()
    }

    private fun setupTable() {
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        scrollPane.verticalScrollBar.addAdjustmentListener {
            if (isScrolledToBottom()) {
                frameViewModel.tableViewModel.loadNextPage()
            }
        }
        addTableToLayout()
    }

    private fun addTableToLayout() {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 2
            weightx = 1.0
            weighty = 1.0
            fill = GridBagConstraints.BOTH
        }
        add(scrollPane, constraints)

        for (i in 0 until table.columnCount) {
            val column = table.columnModel.getColumn(i)
            column.minWidth = DEFAULT_CELL_MIN_WIDTH
            column.preferredWidth = DEFAULT_CELL_WIDTH
        }
    }

    private fun setupStatusText() {
        statusText.border = EmptyBorder(0, 30, 0, 30)
        statusText.font = statusText.font.deriveFont(statusBarTextSize)
        statusText.preferredSize = Dimension(statusText.width, 20)
        addStatusTextToLayout()
    }

    private fun addStatusTextToLayout() {
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            anchor = GridBagConstraints.SOUTHEAST
            weightx = 0.0
            weighty = 0.0
        }
        add(statusText, constraints)
    }

    private fun onStatusUpdated(status: String, type: Status) {
        statusText.text = status
        statusText.foreground = if (type == Status.ERROR) Color.RED else Color.BLACK
    }

    private fun setupSaveShortcut() {
        val saveAction = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                frameViewModel.onSaveClicked()
            }
        }
        val inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        val actionMap = rootPane.actionMap

        val keyStroke = if (System.getProperty("os.name").lowercase().contains("mac")) {
            KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_DOWN_MASK)
        } else {
            KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)
        }

        inputMap.put(keyStroke, "saveAction")
        actionMap.put("saveAction", saveAction)
    }

    private fun createMenuBar() {
        System.setProperty("apple.laf.useScreenMenuBar", "true")
        jMenuBar = EditorMenuBar(frameViewModel)
    }

    private fun isScrolledToBottom(): Boolean {
        val verticalBar = scrollPane.verticalScrollBar
        return verticalBar.value + verticalBar.visibleAmount >= verticalBar.maximum
    }
}
