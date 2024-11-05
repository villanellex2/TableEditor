package com.echernikova.editor.table.model

class TableDependenciesGraph {
    private val dependencies: MutableMap<CellPointer, MutableSet<CellPointer>> = mutableMapOf()
    private val reverseDependencies: MutableMap<CellPointer, MutableSet<CellPointer>> = mutableMapOf()

    /**
     * Evaluates cell which depends on @param cell and should be updated.
     */
    fun getCellsToUpdate(cell: TableCell): DependenciesOutput {
        val output = topologicalSort(cell, dependencies)
        val correctCells = output.pathsToEvaluate.reversed()

        return DependenciesOutput(correctCells, output.cellsInCycle)
    }

    /**
     * Updates dependencies via evaluated result.
     */
    fun updateDependencies(cell: TableCell) {
        val newDependencies = cell.getEvaluationResult().cellDependencies

        clearCellDependencies(cell.pointer)
        newDependencies.forEach { dependency ->
            addDependency(cell.pointer, dependency)
        }
    }

    private fun topologicalSort(
        startCell: TableCell,
        dependencies: Map<CellPointer, MutableSet<CellPointer>>
    ): DependenciesOutput {
        val visited = mutableSetOf<CellPointer>()
        val visiting = mutableSetOf<CellPointer>()
        val sortedCells = mutableListOf<CellPointer>()
        val cyclePath = mutableListOf<CellPointer>()
        val cellsInCycle = mutableSetOf<CellPointer>()

        fun visit(cell: CellPointer) {
            if (cell in visiting) {
                val cycleStartIndex = cyclePath.indexOf(cell)
                val cycle = cyclePath.subList(cycleStartIndex, cyclePath.size)

                sortedCells.removeAll(cycle)
                cellsInCycle.addAll(cycle)
                visited.add(cell)
                return
            }
            if (cell in visited) {
                return
            }

            visiting.add(cell)
            cyclePath.add(cell)
            dependencies[cell]?.forEach { visit(it) }

            visiting.remove(cell)
            visited.add(cell)
            sortedCells.add(cell)
            cyclePath.removeAt(cyclePath.lastIndex)
        }

        visit(startCell.pointer)
        return DependenciesOutput(sortedCells, cellsInCycle.associateWith { this.reverseDependencies[it] })
    }

    private fun addDependency(dependent: CellPointer, dependency: CellPointer) {
        dependencies.getOrPut(dependency) { mutableSetOf() }.add(dependent)
        reverseDependencies.getOrPut(dependent) { mutableSetOf() }.add(dependency)
    }

    private fun clearCellDependencies(cellPointer: CellPointer) {
        reverseDependencies[cellPointer]?.toList()?.forEach { dep ->
            dependencies[dep]?.remove(cellPointer)
        }
        reverseDependencies[cellPointer]?.clear()
    }

    data class DependenciesOutput(
        val pathsToEvaluate: List<CellPointer>,
        val cellsInCycle: Map<CellPointer, Set<CellPointer>?>,
    )
}