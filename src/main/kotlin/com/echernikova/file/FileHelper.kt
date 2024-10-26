package com.echernikova.file

interface FileHelper {
    fun writeTable(table: List<Array<String?>>, filePath: String)
    fun readTable(filePath: String): List<Array<Any?>>?
}