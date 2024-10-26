package com.echernikova.editor

import com.echernikova.editor.table.TableViewModel
import com.echernikova.file.SupportedExtensions
import java.io.File

class EditorViewModel(
    val file: File,
    val tableViewModel: TableViewModel,
) {
    val editedFileName = with(file.path) {
        substring(lastIndexOf('/') + 1, length)
    }
    var onStatusUpdateListener: StatusUpdateListener? = null

    fun onSafeClicked() {
        onStatusUpdateListener?.onStatusUpdated("Saving...", Status.INFO)
        val extension = SupportedExtensions.fromFile(file) ?: run {
            //todo: showError popup
            onStatusUpdateListener?.onStatusUpdated("Can't safe file", Status.ERROR)
            return
        }
        extension.fileHelper.writeTable(tableViewModel.getDataAsList(), file.path)
        onStatusUpdateListener?.onStatusUpdated("Successfully saved", Status.INFO)
    }

    fun loadNewTablePage() {
        tableViewModel.loadNextPage()
    }

    private fun TableViewModel.getDataAsList(): List<Array<String?>> {
        val tableData: MutableList<Array<String?>> = mutableListOf()

        for (row in 0 until rowCount) {
            var hadNotNull = false
            val rowData = Array<String?>(columnCount) { null }

            for (column in 1 until columnCount) {
                getValueAt(row, column)?.let {
                    rowData[column] = it.toString()
                    hadNotNull = true
                }
            }

            if (hadNotNull) {
                rowData[0] = row.toString()
                tableData.add(rowData)
            }
        }

        return tableData
    }
}
