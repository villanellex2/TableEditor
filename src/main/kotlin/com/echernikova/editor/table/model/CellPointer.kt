package com.echernikova.editor.table.model

import kotlin.math.max
import kotlin.math.min

data class CellPointer(val row: Int, val column: Int) {

    override fun toString(): String {
        return "${ 'A' + column - 1 }${ row + 1 }"
    }

    companion object {
        fun fromString(name: String?): CellPointer? {
            name ?: return null
            val column = name[0] - 'A' + 1
            val row = name.substring(1, name.length).toIntOrNull()?.let { it - 1 } ?: return null

            return CellPointer(row, column)
        }


        fun buildCellDependenciesInBetween(from: CellPointer?, to: CellPointer?): Set<CellPointer> {
            from ?: return emptySet<CellPointer>()
            to ?: return emptySet<CellPointer>()
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
