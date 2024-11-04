package com.echernikova.editor

import com.echernikova.editor.table.TableViewModel
import com.echernikova.file.SupportedExtensions
import com.echernikova.fileopening.LastOpenFile
import java.io.File

private const val SAVING_IN_PROCESS = "Saving..."
private const val SAVED_MESSAGE = "Successfully saved."
private val INCORRECT_FILE_TYPE = { extension: String -> "Couldn't save file with type '$extension'." }

class EditorViewModel(
    private val file: File,
    val tableViewModel: TableViewModel,
) {
    var onStatusUpdateListener: StatusUpdateListener? = null

    val editedFileName: String = file.path.substringAfterLast('/')

    fun onSaveClicked() {
        onStatusUpdateListener?.onStatusUpdated(SAVING_IN_PROCESS, Status.INFO)
        val extension = SupportedExtensions.fromFile(file) ?: run {
            onStatusUpdateListener?.onStatusUpdated(INCORRECT_FILE_TYPE(file.extension), Status.ERROR)
            return
        }
        val saveResult = extension.fileHelper.writeTable(
            tableViewModel.getDataAsList(),
            tableViewModel.tableDataController.getEvaluatedCells(),
            file.path
        )

        if (saveResult == null) {
            LastOpenFile.setPath(file.path)
            onStatusUpdateListener?.onStatusUpdated(SAVED_MESSAGE, Status.INFO)
        } else {
            onStatusUpdateListener?.onStatusUpdated(saveResult, Status.ERROR)
        }
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
