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

class FileOpeningFrameViewModel {

    fun onClickOnCreateButton(
        button: JButton,
        disposeFrameCallback: () -> Unit,
    ) {
        val fileChooser = TableFileChooser()
        if (fileChooser.showSaveDialog(button) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile

            if (file.exists()) {
                //todo: showError
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

            if (file.exists()) {
                if (!openExistingTable(file, disposeFrameCallback)) {
                    // todo: showError
                }
            } else {
                //todo: showError
            }
        }
    }

    fun onClickOnOpenRecentButton(
        lastOpenedPath: String,
        disposeFrameCallback: () -> Unit,
    ) {
        val file = File(lastOpenedPath)

        if (file.exists()) {
            if (!openExistingTable(file, disposeFrameCallback)) {
                // todo: showError
            }
        } else {
            // todo: show error
        }
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
    ): Boolean {
        val fileHelper = SupportedExtensions.fromFile(file)?.fileHelper ?: return false
        val data = fileHelper.readTable(file.path) ?: return false

        val editorViewModel: EditorViewModel = getKoin().get { parametersOf(file, data.toTypedArray()) }

        EditorFrame(editorViewModel).isVisible = true
        disposeFrameCallback.invoke()
        return true
    }
}