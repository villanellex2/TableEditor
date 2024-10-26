package com.echernikova.fileopening

import com.echernikova.utils.SystemDirsHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

private const val LAST_OPENED_FILE_CACHE = "lastOpenedFile.properties"

object LastOpenFile {
    private var _evaluatedPath: String? = null

    fun getPath() : String? {
        if (_evaluatedPath != null) return _evaluatedPath
        val cacheDir = SystemDirsHelper.cacheDirectory
        val propertiesFile = File(cacheDir, LAST_OPENED_FILE_CACHE)

        if (propertiesFile.exists()) {
            val properties = Properties()
            FileInputStream(propertiesFile).use { inputStream ->
                properties.load(inputStream)
            }
            return properties.getProperty("lastOpenedFile").also {
                _evaluatedPath = it
            }
        }
        return null
    }

    fun setPath(filePath: String) {
        _evaluatedPath = filePath
        val cacheDir = SystemDirsHelper.cacheDirectory
        val propertiesFile = File(cacheDir, LAST_OPENED_FILE_CACHE)

        val properties = Properties()
        properties.setProperty("lastOpenedFile", filePath)

        FileOutputStream(propertiesFile).use { outputStream ->
            properties.store(outputStream, "Last Opened File")
        }
    }

}