package com.echernikova.fileopening

import com.echernikova.views.RoundedButton
import java.awt.*
import javax.swing.*

private const val FRAME_NAME = "Open table"

class FileOpeningFrame(
    private val viewModel: FileOpeningFrameViewModel,
) : JFrame() {

    init {
        title = FRAME_NAME
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false
        setSize(600, 480)
        preferredSize = Dimension(600, 480)

        setLocationRelativeTo(null)
        layout = GridBagLayout()
        val constraints = GridBagConstraints()
        constraints.insets = Insets(10, 10, 10, 10)
        constraints.anchor = GridBagConstraints.CENTER

        constraints.gridx = 0
        constraints.gridy = 0
        constraints.gridwidth = 2
        constraints.gridheight = 1
        constraints.weightx = 0.0
        constraints.weighty = 0.0
        constraints.fill = GridBagConstraints.NONE
        val openResentButton = createOpenResentButton()
        openResentButton.preferredSize = Dimension(440, 100)
        add(openResentButton, constraints)

        constraints.gridy = 1
        constraints.gridwidth = 1
        val createButton = createCreateButton()
        createButton.preferredSize = Dimension(200, 200)
        add(createButton, constraints)

        constraints.gridx = 1
        val openButton = createOpenButton()
        openButton.preferredSize = Dimension(200, 200)
        add(openButton, constraints)

    }

    private fun createCreateButton() = createButtonBase("<html>Create<br>new table</html>").apply {
        background = FileOpeningFrameTheme.createButtonBackgroundColor
        addActionListener {
            viewModel.onClickOnCreateButton(this) { this@FileOpeningFrame.dispose() }
        }
    }

    private fun createOpenButton() = createButtonBase("<html>Open<br>existing table</html>").apply {
        background = FileOpeningFrameTheme.openButtonBackgroundColor
        addActionListener {
            viewModel.onClickOnOpenButton(this) { this@FileOpeningFrame.dispose() }
        }
    }

    private fun createOpenResentButton(): JButton {
        val path = LastOpenFile.getPath()
        return createButtonBase(
            "<html>" +
                    "<div style='text-align: left;'>Open recent file </div>" +
                    "<div style='font-size: 6px'></div>" +
                    "<div style='font-size: 11px; color: rgb(100,100,100); font-family: monospace;'>" +
                    (path ?: "no recent path..") + "</div></html>", path != null
        ).apply {
            horizontalAlignment = SwingConstants.LEFT
            if (path != null ) {
                addActionListener {
                    viewModel.onClickOnOpenRecentButton(path) { this@FileOpeningFrame.dispose() }
                }
                background = FileOpeningFrameTheme.openLastButtonBackgroundColorActive
            } else {
                background = FileOpeningFrameTheme.openLastButtonBackgroundColorInactive
            }
        }
    }

    private fun createButtonBase(text: String, isActive: Boolean = true) = RoundedButton(text, 25).apply {
        font = font.deriveFont(FileOpeningFrameTheme.buttonsFontSize)
        foreground = FileOpeningFrameTheme.buttonFontColor
        horizontalAlignment = SwingConstants.CENTER

        if (isActive) {
            addMouseListener(object : java.awt.event.MouseAdapter() {
                override fun mouseEntered(e: java.awt.event.MouseEvent?) {
                    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                }

                override fun mouseExited(e: java.awt.event.MouseEvent?) {
                    cursor = Cursor.getDefaultCursor()
                }
            })
        }
    }
}
