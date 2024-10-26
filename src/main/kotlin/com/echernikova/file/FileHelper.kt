package com.echernikova.file

import com.echernikova.editor.table.model.TableCell

interface FileHelper {
    /**
     * Returns error message if error occurred on saving.
     */
    fun writeTable(
        table: List<Pair<Int, Array<TableCell?>>> ,
        filePath: String
    ): String?

    fun readTable(filePath: String): List<Array<String?>>?
}