package com.echernikova.editor

import com.echernikova.editor.table.TableViewModel
import com.echernikova.editor.table.model.TableDataController
import com.echernikova.file.SupportedExtensions
import com.echernikova.fileopening.LastOpenFile
import java.io.File

private const val SAVING_IN_PROCESS = "Saving..."
private const val SAVED_MESSAGE = "Successfully saved."
private val INCORRECT_FILE_TYPE = { extension: String -> "Couldn't save file with type '$extension'." }

class EditorViewModel(
    private val file: File,
    initialData: Array<Array<Any?>>?,
) {
    val tableViewModel: TableViewModel = TableViewModel(initialData, TableDataController())
    val editedFileName: String = file.path.substringAfterLast('/')

    fun onSaveClicked(onStatusUpdateListener: StatusUpdateListener) {
        onStatusUpdateListener.onStatusUpdated(SAVING_IN_PROCESS, Status.INFO)
        val extension = SupportedExtensions.fromFile(file) ?: run {
            onStatusUpdateListener.onStatusUpdated(INCORRECT_FILE_TYPE(file.extension), Status.ERROR)
            return
        }

        //todo: сейв в корутину
        val saveResult = extension.fileHelper.writeTable(
            tableViewModel.getDataAsList(),
            tableViewModel.tableDataController.getEvaluatedCells(),
            file.path
        )

        val (result, status) = if (saveResult == null) {
            LastOpenFile.setPath(file.path)
            SAVED_MESSAGE to Status.INFO
        } else {
            saveResult to Status.ERROR
        }

        onStatusUpdateListener.onStatusUpdated(result, status)
    }
}

private fun TableViewModel.getDataAsList(): List<Array<String?>> {
    val tableData = mutableListOf<Array<String?>>()

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
