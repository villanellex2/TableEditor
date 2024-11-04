package com.echernikova.editor

import com.echernikova.editor.table.TableViewModel
import com.echernikova.file.SupportedExtensions
import com.echernikova.fileopening.LastOpenFile
import java.io.File
import javax.swing.JOptionPane

private const val SAVING_IN_PROCESS = "Saving..."
private const val SAVED_MESSAGE = "Successfully saved."
private const val ERROR_ON_SAVING = "Error on saving file."
private val INCORRECT_FILE_TYPE = { extension: String -> "Couldn't save file with type '$extension'."}

class EditorViewModel(
    val file: File,
    val tableViewModel: TableViewModel,
) {
    val editedFileName = with(file.path) {
        substring(lastIndexOf('/') + 1, length)
    }
    var onStatusUpdateListener: StatusUpdateListener? = null

    fun onSafeClicked() {
        onStatusUpdateListener?.onStatusUpdated(SAVING_IN_PROCESS, Status.INFO)
        val extension = SupportedExtensions.fromFile(file) ?: run {
            showErrorMessage(INCORRECT_FILE_TYPE(file.extension))
            onStatusUpdateListener?.onStatusUpdated(ERROR_ON_SAVING, Status.ERROR)
            return
        }
        val saveResult = extension.fileHelper.writeTable(tableViewModel.getDataAsList(), file.path)

        if (saveResult == null) {
            LastOpenFile.setPath(file.path)
            onStatusUpdateListener?.onStatusUpdated(SAVED_MESSAGE, Status.INFO)
        } else {
            onStatusUpdateListener?.onStatusUpdated(ERROR_ON_SAVING, Status.ERROR)
            showErrorMessage(saveResult.localizedMessage)
        }
    }

    private fun showErrorMessage(message: String?) {
        JOptionPane.showMessageDialog(
            /* parentComponent = */ null,
            /* message = */ message ?: ERROR_ON_SAVING,
            /* title = */ "Cannot save file",
            /* messageType = */ JOptionPane.ERROR_MESSAGE
        )
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
