package com.echernikova.file

import java.io.File

enum class SupportedExtensions(
    val extension: String,
    val fileHelper: FileHelper,
) {

    XLS("xls", XSSFileHelper());

    companion object {
        fun fromFile(file: File): SupportedExtensions? {
            val fileExtension = file.extension.lowercase()
            return entries.find { it.extension == fileExtension }
        }
    }
}