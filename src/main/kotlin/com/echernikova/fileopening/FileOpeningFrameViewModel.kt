package com.echernikova.fileopening

import com.echernikova.file.SupportedExtensions
import com.echernikova.file.TableFileChooser
import com.echernikova.editor.EditorFrame
import com.echernikova.editor.EditorViewModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.getKoin
import java.io.File
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JOptionPane

class FileOpeningFrameViewModel {

    fun getRecentOpenFile() = LastOpenFile.getPath()

    fun onClickOnCreateButton(
        button: JButton,
        disposeFrameCallback: () -> Unit,
    ) {
        val fileChooser = TableFileChooser()
        if (fileChooser.showSaveDialog(button) == JFileChooser.APPROVE_OPTION) {
            var file = fileChooser.selectedFile

            if (file.extension.isNullOrEmpty()) {
                file = File("${file.path}.xls")
            }

            if (file.exists()) {
                showErrorDialog("File ${file.path} already exists.", "Cannot create file.")
            } else {
                openEmptyTable(file, disposeFrameCallback)
            }
        }
    }

    fun onClickOnOpenButton(
        button: JButton,
        disposeFrameCallback: () -> Unit,
    ) {
        val fileChooser = TableFileChooser()
        if (fileChooser.showOpenDialog(button) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            LastOpenFile.setPath(file.path)

            openExistingFile(file, disposeFrameCallback)
        }
    }

    fun onClickOnOpenRecentButton(
        lastOpenedPath: String,
        disposeFrameCallback: () -> Unit,
    ): Boolean {
        return openExistingFile( File(lastOpenedPath), disposeFrameCallback)
    }

    private fun openExistingFile(
        file: File,
        disposeFrameCallback: () -> Unit,
    ): Boolean {
        return if (file.exists()) {
            when (openExistingTable(file, disposeFrameCallback)) {
                FileOpeningStatus.SUCCESS -> true
                FileOpeningStatus.CANNOT_READ -> {
                    showErrorDialog("Cannot read data in ${file.path}.", "Can't open file.")
                    false
                }
                FileOpeningStatus.UNSUPPORTED_EXTENSION -> {
                    showErrorDialog("Unsupported file type ${file.path}.", "Can't open file.")
                    false
                }
                FileOpeningStatus.ERROR_ON_TABLE_READING -> {
                    showErrorDialog("Incorrect table data. Error on reading.", "Can't open file.")
                    false
                }
            }
        } else {
            showErrorDialog("File is not exist.", "Can't open file.")
            false
        }
    }

    private fun showErrorDialog(
        message: String,
        title: String,
    ) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)
    }

    private fun openEmptyTable(
        file: File,
        disposeFrameCallback: () -> Unit,
    ) {
        val editorViewModel: EditorViewModel = getKoin().get { parametersOf(file, null) }

        EditorFrame(editorViewModel).isVisible = true
        disposeFrameCallback.invoke()
    }

    private fun openExistingTable(
        file: File,
        disposeFrameCallback: () -> Unit,
    ): FileOpeningStatus {
        if (!file.canRead()) return FileOpeningStatus.CANNOT_READ
        val fileHelper = SupportedExtensions.fromFile(file)?.fileHelper ?: return FileOpeningStatus.UNSUPPORTED_EXTENSION
        val data = fileHelper.readTable(file.path) ?: return FileOpeningStatus.ERROR_ON_TABLE_READING

        val editorViewModel: EditorViewModel = getKoin().get { parametersOf(file, data.toTypedArray()) }

        EditorFrame(editorViewModel).isVisible = true
        disposeFrameCallback.invoke()
        return FileOpeningStatus.SUCCESS
    }

    private enum class FileOpeningStatus() {
        SUCCESS,
        ERROR_ON_TABLE_READING,
        UNSUPPORTED_EXTENSION,
        CANNOT_READ
    }
}