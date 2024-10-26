package com.echernikova.utils

import java.io.File

object SystemDirsHelper {
    val documentsDirectory: File by lazy {
        File(System.getProperty("user.home"), "Documents")
    }

    val cacheDirectory: File by lazy {
        val osName = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")

        val cacheDir = when {
            osName.contains("win") -> File(System.getenv("APPDATA"), "MyApp")
            osName.contains("mac") -> File(userHome, "Library/Application Support/MyApp")
            else -> File(userHome, ".cache/MyApp")
        }

        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        cacheDir
    }
}
