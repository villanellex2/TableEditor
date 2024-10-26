package com.echernikova.file

import com.echernikova.fileopening.LastOpenFile
import com.echernikova.utils.SystemDirsHelper
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class TableFileChooser(): JFileChooser() {
    init {
        currentDirectory = LastOpenFile.getPath()?.let { file -> File(file) } ?: SystemDirsHelper.documentsDirectory

        dialogType = OPEN_DIALOG
        fileSelectionMode = FILES_ONLY
        isAcceptAllFileFilterUsed = false
        val supportedExtensions = SupportedExtensions.entries.map { it.extension }.toTypedArray()
        fileFilter = FileNameExtensionFilter("XLS", *supportedExtensions)
    }
}