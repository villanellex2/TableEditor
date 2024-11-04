package com.echernikova.file

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.evaluator.core.EvaluationResult

interface FileHelper {
    /**
     * Returns error message if error occurred on saving.
     */
    fun writeTable(
        table: List<Array<String?>>,
        mapEvaluated: Map<CellPointer, EvaluationResult<*>>,
        filePath: String
    ): String?

    fun readTable(filePath: String): List<Array<Any?>>?
}