package com.echernikova.fileopening

import com.echernikova.file.TableFileChooser
import com.echernikova.views.RoundedButton
import java.awt.*
import java.io.File
import javax.swing.*

private const val FRAME_NAME = "Open table"

class FileOpeningFrame(
    private val viewModel: FileOpeningFrameViewModel,
) : JFrame() {

    init {
        setupFrame()
        val constraints = GridBagConstraints().apply {
            insets = Insets(10, 10, 10, 10)
            anchor = GridBagConstraints.CENTER
        }

        addButtons(constraints)
    }

    private fun setupFrame() {
        title = FRAME_NAME
        defaultCloseOperation = EXIT_ON_CLOSE
        isResizable = false
        setSize(600, 480)
        preferredSize = Dimension(600, 480)
        setLocationRelativeTo(null)
        layout = GridBagLayout()
    }

    private fun addButtons(constraints: GridBagConstraints) {
        constraints.apply {
            gridx = 0
            gridwidth = 2
            add(createOpenRecentButton().apply { preferredSize = Dimension(440, 100) }, this)

            gridwidth = 1
            gridy = 1
            add(createFileActionButton("<html>Create<br>new table</html>", FileOpeningFrameTheme.createButtonBackgroundColor) {
                handleCreateFileAction()
            }.apply { preferredSize = Dimension(200, 200) }, this)

            gridx = 1
            add(createFileActionButton("<html>Open<br>existing table</html>", FileOpeningFrameTheme.openButtonBackgroundColor) {
                handleOpenFileAction()
            }.apply { preferredSize = Dimension(200, 200) }, this)
        }
    }

    private fun handleCreateFileAction() {
        val fileChooser = TableFileChooser()
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile.let { if (it.extension.isEmpty()) File("${it.path}.xls") else it }
            when (viewModel.onCreateNewTable(file)) {
                FileOpeningFrameViewModel.FileOpeningStatus.FILE_EXISTS -> showErrorDialog("File ${file.path} already exists.", "Cannot create file.")
                FileOpeningFrameViewModel.FileOpeningStatus.SUCCESS -> dispose()
                else -> showErrorDialog("Unknown error occurred.", "Error")
            }
        }
    }

    private fun handleOpenFileAction() {
        val fileChooser = TableFileChooser()
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            when (viewModel.onOpenExistingTable(file)) {
                FileOpeningFrameViewModel.FileOpeningStatus.FILE_NOT_FOUND -> showErrorDialog("File does not exist.", "Can't open file.")
                FileOpeningFrameViewModel.FileOpeningStatus.CANNOT_READ -> showErrorDialog("Cannot read data in ${file.path}.", "Can't open file.")
                FileOpeningFrameViewModel.FileOpeningStatus.ERROR_ON_TABLE_READING -> showErrorDialog("Incorrect table data. Error on reading.", "Can't open file.")
                FileOpeningFrameViewModel.FileOpeningStatus.SUCCESS -> dispose()
                else -> showErrorDialog("Unsupported file type ${file.path}.", "Can't open file.")
            }
        }
    }

    private fun createOpenRecentButton(): JButton {
        val path = viewModel.getRecentOpenFile()
        val buttonText = "<html>Open recent file<div style='font-size: 11px; color: rgb(100,100,100); font-family: monospace;'>${path ?: "no recent path.."}</div></html>"

        return createButtonBase(buttonText, path != null).apply {
            horizontalAlignment = SwingConstants.LEFT
            background = if (path != null && File(path).exists()) {
                addActionListener {
                    when (viewModel.onOpenExistingTable(File(path))) {
                        FileOpeningFrameViewModel.FileOpeningStatus.SUCCESS -> dispose()
                        else -> background = FileOpeningFrameTheme.openLastButtonBackgroundColorInactive
                    }
                }
                FileOpeningFrameTheme.openLastButtonBackgroundColorActive
            } else {
                FileOpeningFrameTheme.openLastButtonBackgroundColorInactive
            }
        }
    }

    private fun createFileActionButton(text: String, bgColor: Color, action: () -> Unit): JButton {
        return createButtonBase(text, true).apply {
            background = bgColor
            addActionListener { action() }
        }
    }

    private fun createButtonBase(text: String, isActive: Boolean) = RoundedButton(text, 25).apply {
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

    private fun showErrorDialog(message: String, title: String) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE)
    }
}
