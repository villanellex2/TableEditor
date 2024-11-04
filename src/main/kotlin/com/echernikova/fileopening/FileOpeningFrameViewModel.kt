package com.echernikova.fileopening

import com.echernikova.editor.EditorFrame
import com.echernikova.editor.EditorViewModel
import com.echernikova.file.SupportedExtensions
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.getKoin
import java.io.File


class FileOpeningFrameViewModel {

    fun getRecentOpenFile() = LastOpenFile.getPath()

    fun onCreateNewTable(file: File): FileOpeningStatus {
        return if (file.exists()) {
            FileOpeningStatus.FILE_EXISTS
        } else {
            openEmptyTable(file)
            FileOpeningStatus.SUCCESS
        }
    }

    fun onOpenExistingTable(file: File): FileOpeningStatus {
        LastOpenFile.setPath(file.path)
        return openExistingFile(file)
    }

    private fun openExistingFile(file: File): FileOpeningStatus {
        return if (file.exists()) {
            when {
                !file.canRead() -> FileOpeningStatus.CANNOT_READ
                SupportedExtensions.fromFile(file)?.fileHelper?.readTable(file.path) == null -> FileOpeningStatus.ERROR_ON_TABLE_READING
                else -> {
                    openExistingTable(file)
                    FileOpeningStatus.SUCCESS
                }
            }
        } else {
            FileOpeningStatus.FILE_NOT_FOUND
        }
    }

    private fun openEmptyTable(file: File) {
        val editorViewModel: EditorViewModel = getKoin().get { parametersOf(file, null) }

        EditorFrame(editorViewModel).isVisible = true
    }

    private fun openExistingTable(file: File) {
        val fileHelper = SupportedExtensions.fromFile(file)?.fileHelper!!
        val data = fileHelper.readTable(file.path)!!.toTypedArray()
        val editorViewModel: EditorViewModel = getKoin().get { parametersOf(file, data) }
        EditorFrame(editorViewModel).isVisible = true
    }

    enum class FileOpeningStatus {
        SUCCESS,
        FILE_EXISTS,
        FILE_NOT_FOUND,
        CANNOT_READ,
        ERROR_ON_TABLE_READING
    }
}
