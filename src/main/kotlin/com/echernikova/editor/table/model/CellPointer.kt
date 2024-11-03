package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.EvaluationException
import kotlin.math.max
import kotlin.math.min

data class CellPointer(val row: Int, val column: Int) {
    companion object {
        fun fromString(name: String): CellPointer {
            val column = name[0] - 'A' + 1
            if (column < 0 || column >= 21) throw EvaluationException("Cell link $name is incorrect.")
            val row = name.substring(1, name.length).toIntOrNull()
                ?: throw EvaluationException("Cell link $name is incorrect.")

            return CellPointer(row, column)
        }


        fun buildCellDependenciesInBetween(from: CellPointer, to: CellPointer): MutableSet<CellPointer> {
            val (row1, column1) = from
            val (row2, column2) = to

            val dependencies = mutableSetOf<CellPointer>()

            for (i in min(row1, row2)..max(row1, row2)) {
                for (j in min(column1, column2)..max(column1, column2)) {
                    dependencies.add(CellPointer(i, j))
                }
            }
            return dependencies
        }
    }
}
