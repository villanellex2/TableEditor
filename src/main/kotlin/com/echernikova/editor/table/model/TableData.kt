package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.Evaluator
import java.util.*

// todo: добавить топологическую сортировку при триггере коллбеков?
// todo: коллбеки точно должны храниться где-нибудь не внутри таблиц, чтобы для пустых ячеек не инитить ячейки... или норм?
class TableData(
    sharingVector: Vector<Vector<Any?>>,
    private val evaluator: Evaluator,
    private val cellExpiredCallback: (Int, Int) -> Unit,
) {
    private val data = sharingVector.mapIndexed { row, vector ->
        val map = mutableMapOf<Int, TableCell>()
        vector.forEachIndexed { column, value ->
            if (column != 0) {
                if (!value?.toString().isNullOrEmpty()) {
                    val cell = createCell(row, column, value.toString())
                    map[column] = cell
                    cell.evaluate()
                }
            }
        }

        map
    }.toMutableList()

    fun getCell(row: Int, column: Int): TableCell? {
        if (row < 0 || column < 0) return null
        if (data.size > row) {
            return data[row][column]
        }
        return null
    }

    fun getOrCreateCell(row: Int, column: Int): TableCell? {
        if (column == 0) return null
        return getCell(row, column) ?: createCell(row, column).also {
            data[row][column] = it
        }
    }

    fun setValueToCell(row: Int, column: Int, value: String?) {
        getCell(row, column)?.also {
            it.rawValue = value
        } ?: run {
            createCell(row, column, value).also {
                data[row][column] = it
            }
        }
    }

    fun addRow(row: Int, values: Array<Any?>) {
        data.add(
            values.mapIndexedNotNull { i, value ->
            if (i == 0 || value == null) null else createCell(row, i, value.toString())
        }.associateBy { it.column }.toMutableMap()
        )
    }

    private fun createCell(row: Int, column: Int, value: String? = null) = TableCell(
        initialValue = value,
        row = row,
        column = column,
        tableData = this,
        evaluator = evaluator,
        cellExpiredCallback = cellExpiredCallback
    )
}
