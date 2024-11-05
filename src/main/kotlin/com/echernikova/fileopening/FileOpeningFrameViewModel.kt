package com.echernikova.fileopening

import com.echernikova.editor.EditorFrame
import com.echernikova.editor.EditorViewModel
import com.echernikova.file.SupportedExtensions
import java.io.File


class FileOpeningFrameViewModel {

    fun getRecentOpenFile() = LastOpenFile.getPath()

    fun onCreateNewTable(file: File): FileOpeningStatus {
        return if (file.exists()) {
            FileOpeningStatus.FILE_EXISTS
        } else {
            openTable(file)
            FileOpeningStatus.SUCCESS
        }
    }

    fun onOpenExistingTable(file: File): FileOpeningStatus {
        LastOpenFile.setPath(file.path)
        return openExistingFile(file)
    }

    private fun openExistingFile(file: File): FileOpeningStatus {
        if (!file.exists()) return FileOpeningStatus.FILE_NOT_FOUND
        if (!file.canRead()) return FileOpeningStatus.CANNOT_READ

        SupportedExtensions.fromFile(file)?.fileHelper?.readTable(file.path)?.let { openTable(file, it) }
            ?: return FileOpeningStatus.ERROR_ON_TABLE_READING

        return FileOpeningStatus.SUCCESS
    }

    private fun openTable(file: File, data: List<Array<Any?>>? = null) {
        val editorViewModel = EditorViewModel(file, data?.toTypedArray())
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
