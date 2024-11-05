package com.echernikova.editor

import com.echernikova.editor.table.TableTheme
import com.echernikova.editor.table.TableView
import com.echernikova.fileopening.FileOpeningFrame
import com.echernikova.fileopening.FileOpeningFrameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.java.KoinJavaComponent.getKoin
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.border.EmptyBorder

private const val DEFAULT_CELL_MIN_WIDTH = 30
private const val DEFAULT_CELL_WIDTH = 120

class EditorFrame(
    private val frameViewModel: EditorViewModel,
) : JFrame() {
    private val table = TableView(frameViewModel.tableViewModel)
    private val scrollPane = JScrollPane(table)
    private val statusText = JLabel()

    private val scope = CoroutineScope(Dispatchers.Default)
    private val statusUpdateListener = StatusUpdateListener(::onStatusUpdated, scope)

    init {
        setupFrame()
        scrollPane.setupScrollBar()
        statusText.setupStatusText()
        setupSaveShortcut()

        addWindowListener(object : java.awt.event.WindowAdapter() {
            override fun windowClosing(e: java.awt.event.WindowEvent) {
                FileOpeningFrame(getKoin().get<FileOpeningFrameViewModel>()).isVisible = true
                super.windowClosing(e)
            }
        })

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

    private fun JScrollPane.setupScrollBar() {
        horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        verticalScrollBar.addAdjustmentListener {
            if (isScrolledToBottom()) {
                frameViewModel.tableViewModel.loadNextPage()
            }
        }
        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 2
            weightx = 1.0
            weighty = 1.0
            fill = GridBagConstraints.BOTH
        }
        this@EditorFrame.add(this, constraints)

        for (i in 0 until table.columnCount) {
            val column = table.columnModel.getColumn(i)
            column.minWidth = DEFAULT_CELL_MIN_WIDTH
            column.preferredWidth = DEFAULT_CELL_WIDTH
        }
    }

    private fun JLabel.setupStatusText() {
        border = EmptyBorder(0, 30, 0, 30)
        font = statusText.font.deriveFont(TableTheme.currentTheme.statusBarTextSize)

        val constraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            anchor = GridBagConstraints.SOUTHEAST
            weightx = 0.0
            weighty = 0.0
        }
        this@EditorFrame.add(this, constraints)
    }

    private fun onStatusUpdated(status: String, type: Status) {
        statusText.text = status
        statusText.foreground = if (type == Status.ERROR) Color.RED else Color.BLACK
    }

    private fun setupSaveShortcut() {
        val saveAction = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                frameViewModel.onSaveClicked(statusUpdateListener)
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
        jMenuBar = EditorMenuBar(frameViewModel, statusUpdateListener)
    }

    private fun isScrolledToBottom(): Boolean {
        val verticalBar = scrollPane.verticalScrollBar
        return verticalBar.value + verticalBar.visibleAmount >= verticalBar.maximum
    }
}
