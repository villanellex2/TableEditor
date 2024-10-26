package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.Evaluator
import java.util.*

// todo: добавить топологическую сортировку при триггере коллбеков?
class TableData(
    sharingVector: Vector<Vector<Any?>>,
    private val evaluator: Evaluator,
    private val cellExpiredCallback: (Int, Int) -> Unit,
) {
    private val data = sharingVector.mapIndexed { row, vector ->
        vector.mapIndexed { column, value ->
            TableCell(
                initialValue = value?.toString(),
                row = row,
                column = column,
                tableData = this,
                evaluator = evaluator,
                cellExpiredCallback = cellExpiredCallback
            )
        }
    }.toMutableList()

    fun getCell(row: Int, column: Int): TableCell? {
        if (row < 0 || column < 0) return null
        if (data.size > row && data[row].size > column) {
            return data[row][column]
        }
        return null
    }

    fun addRow(row: Int, values: Array<Any?>) {
        data.add(
            values.mapIndexed { i, value ->
                TableCell(
                    initialValue = value?.toString(),
                    row = row,
                    column = i,
                    tableData = this,
                    evaluator = evaluator,
                    cellExpiredCallback = cellExpiredCallback
                )
            }
        )
    }
}
