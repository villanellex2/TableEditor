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

class EditorFrame(
    private val frameViewModel: EditorViewModel,
) : JFrame() {
    private val table = TableView(frameViewModel.tableViewModel)
    private val scrollPane = JScrollPane(table)
    private val statusText = JLabel()

    init {
        title = frameViewModel.editedFileName

        defaultCloseOperation = DISPOSE_ON_CLOSE
        setSize(1200, 800)
        setLocationRelativeTo(null)

        layout = GridBagLayout()

        val constraints = GridBagConstraints()
        constraints.insets = Insets(0, 0, 0, 0)
        constraints.anchor = GridBagConstraints.CENTER

        createMenuBar()
        setupSaveShortcut()
        configureScrollableTable()

        constraints.gridx = 0
        constraints.gridy = 0
        constraints.gridwidth = 2
        constraints.gridheight = 1
        constraints.weightx = 1.0
        constraints.weighty = 1.0
        constraints.fill = GridBagConstraints.BOTH
        add(scrollPane, constraints)

        constraints.gridy = 1
        constraints.weightx = 0.0
        constraints.weighty = 0.0
        constraints.fill = GridBagConstraints.NONE
        constraints.anchor = GridBagConstraints.SOUTHEAST
        configureStatusText()
        add(statusText, constraints)

        addWindowListener(object : java.awt.event.WindowAdapter() {
            override fun windowClosing(e: java.awt.event.WindowEvent) {
                FileOpeningFrame(getKoin().get<FileOpeningFrameViewModel>()).isVisible = true
                super.windowClosing(e)
            }
        })

        frameViewModel.onStatusUpdateListener = StatusUpdateListener(::onStatusUpdated, CoroutineScope(Dispatchers.Default))
    }

    private fun onStatusUpdated(status: String, type: Status) {
        statusText.text = status
        statusText.foreground = when(type) {
            Status.ERROR -> Color.RED
            Status.INFO -> Color.BLACK
        }
    }

    private fun createMenuBar() {
        // todo: SafeAs(по новому пути)
        System.setProperty("apple.laf.useScreenMenuBar", "true")
        jMenuBar = EditorMenuBar(frameViewModel)
    }

    private fun configureScrollableTable() {
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED

        scrollPane.verticalScrollBar.addAdjustmentListener {
            if (isScrolledToBottom()) {
                frameViewModel.loadNewTablePage()
            }
        }
    }

    private fun isScrolledToBottom(): Boolean {
        val verticalBar = scrollPane.verticalScrollBar
        return verticalBar.value + verticalBar.visibleAmount >= verticalBar.maximum
    }

    private fun configureStatusText() {
        statusText.border = EmptyBorder(0, 30, 0, 30)
        statusText.font = statusText.font.deriveFont(TableTheme.statusBarTextSize)
    }

    private fun setupSaveShortcut() {
        val saveAction = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                frameViewModel.onSafeClicked()
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
}
