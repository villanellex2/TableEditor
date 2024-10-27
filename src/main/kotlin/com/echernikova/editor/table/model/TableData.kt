package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.Evaluator
import java.util.*

// todo: добавить топологическую сортировку при триггере коллбеков?
// todo: коллбеки точно должны храниться где-нибудь не внутри таблиц, чтобы для пустых ячеек не инитить ячейки... или норм?
class TableData(
    private val evaluator: Evaluator,
) {
    private var data: MutableMap<CellPointer, TableCell> = mutableMapOf()
    private var dataExpiredCallback: ((CellPointer) -> Unit) = {}

    fun initData(sharingVector: Vector<Vector<Any?>>) {
        assert(data.isEmpty()) { "Data should be empty on init" }

        sharingVector.forEachIndexed { row, vector ->
            vector.forEachIndexed { column, value ->
                if (value != null) {
                    data[CellPointer(row, column)] = createCell(CellPointer(row, column), value.toString())
                }
            }
        }
        data.forEach { (i, value) -> value.evaluate() }
    }

    fun setOnDataExpiredCallback(callback: (CellPointer) -> Unit) {
        dataExpiredCallback = callback
    }

    fun getCell(pointer: CellPointer): TableCell? = data[pointer]

    fun getOrCreateCell(pointer: CellPointer): TableCell? {
        return getCell(pointer) ?: createCell(pointer).also {
            data[pointer] = it
        }
    }

    fun setValueToCell(pointer: CellPointer, value: String?) {
        getCell(pointer)?.also {
            it.rawValue = value
        } ?: run {
            createCell(pointer, value).also {
                data[pointer] = it
                it.evaluate()
                dataExpiredCallback.invoke(pointer)
            }
        }
    }

    fun addRow(row: Int, values: Array<Any?>) {
        values.mapIndexedNotNull { i, value ->
            if (i == 0 || value == null) null else data[CellPointer(row, i)] = createCell(CellPointer(row, i), value.toString())
        }
    }

    private fun createCell(pointer: CellPointer, value: String? = null) = TableCell(
        initialValue = value,
        cellPointer = pointer,
        tableData = this,
        evaluator = evaluator,
        cellExpiredCallback = dataExpiredCallback
    )
}
