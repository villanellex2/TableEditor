package com.echernikova.file

interface FileHelper {
    fun writeTable(table: List<Array<String?>>, filePath: String): Throwable?
    fun readTable(filePath: String): List<Array<Any?>>?
}