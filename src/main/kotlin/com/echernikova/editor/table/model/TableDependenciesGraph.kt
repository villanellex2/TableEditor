package com.echernikova.editor.table.model

class TableDependenciesGraph {
    private val dependencies: MutableMap<CellPointer, MutableSet<CellPointer>> = mutableMapOf()
    private val reverseDependencies: MutableMap<CellPointer, MutableSet<CellPointer>> = mutableMapOf()

    /**
     * Evaluates cell which depends on @param cell and should be updated.
     */
    fun getCellsToUpdate(cell: TableCell) =
        topologicalSort(cell, dependencies).reversed().let { it.subList(1, it.size) }

    /**
     * Evaluates cells that should be evaluated before @param cell, because it depends on them.
     */
    fun getCellsToEvaluate(cell: TableCell) = topologicalSort(cell, reverseDependencies)

    /**
     * Initial dependencies creation before result evaluation.
     * Because evaluation could be broken by incorrect tokens etc., and
     * unnecessary recalculation of values could be avoided.
     */
    fun initDependencies(cell: TableCell) {
        clearCellDependencies(cell.cellPointer)
        if (cell.rawValue.isNullOrEmpty() || cell.rawValue?.startsWith("=") == false) return

        val newDependencies = extractDependenciesFromFormula(cell.rawValue)
        newDependencies.forEach { dependency ->
            addDependency(cell.cellPointer, dependency)
        }
    }

    /**
     * Updates dependencies via evaluated result.
     */
    fun updateDependencies(cell: TableCell) {
        val newDependencies = cell.evaluationResult?.cellDependencies ?: return

        clearCellDependencies(cell.cellPointer)
        newDependencies.forEach { dependency ->
            addDependency(cell.cellPointer, dependency)
        }
    }

    private fun topologicalSort(
        startCell: TableCell,
        dependencies: Map<CellPointer, MutableSet<CellPointer>>
    ): List<CellPointer> {
        val visited = mutableSetOf<CellPointer>()
        val visiting = mutableSetOf<CellPointer>()
        val sortedCells = mutableListOf<CellPointer>()
        val cyclePath = mutableListOf<CellPointer>()

        fun visit(cell: CellPointer) {
            if (cell in visiting) {
                val cycleStartIndex = cyclePath.indexOf(cell)
                val cycle = cyclePath.subList(cycleStartIndex, cyclePath.size)
                throw IllegalStateException("Cycle detected in dependencies involving cells: $cycle") // todo: придумать, как нормально выводить такие пути
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

        visit(startCell.cellPointer)
        return sortedCells
    }

    private fun addDependency(dependent: CellPointer, dependency: CellPointer) {
        dependencies.getOrPut(dependency) { mutableSetOf() }.add(dependent)
        reverseDependencies.getOrPut(dependent) { mutableSetOf() }.add(dependency)
    }

    private fun clearCellDependencies(cellPointer: CellPointer) {
        reverseDependencies[cellPointer]?.forEach { dep ->
            dependencies[dep]?.remove(cellPointer)
        }
        reverseDependencies[cellPointer]?.clear()
    }

    /**
     * Used only to evaluate initial dependencies.
     * Otherwise, evaluated dependencies will be used to avoid unnecessary recalculations.
     */
    private fun extractDependenciesFromFormula(formula: String?): Set<CellPointer> {
        if (formula.isNullOrEmpty() || !formula.startsWith("=")) return emptySet()

        val regex = Regex("([A-Z][0-9]+)(?::([A-Z][0-9]+))?")
        val dependencies = mutableSetOf<CellPointer>()

        regex.findAll(formula).forEach { matchResult ->
            val startCell = matchResult.groupValues[1]
            val endCell = matchResult.groupValues.getOrNull(2)

            if (endCell.isNullOrEmpty()) {
                dependencies.add(CellPointer.fromString(startCell))
            } else {
                dependencies.addAll(
                    CellPointer.buildCellDependenciesInBetween(
                        CellPointer.fromString(startCell),
                        CellPointer.fromString(endCell)
                    )
                )
            }
        }
        return dependencies
    }

    class TopologicalSortOutput(
        correctPath: List<CellPointer>,
        firstCycle: List<CellPointer>,
    )
}