package com.echernikova.editor

import com.echernikova.editor.table.model.EvaluatingTableModel
import com.echernikova.editor.table.model.TableCell
import com.echernikova.evaluator.core.Evaluator
import com.echernikova.file.SupportedExtensions
import com.echernikova.fileopening.LastOpenFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import java.io.File
import javax.swing.SwingUtilities

private const val SAVING_IN_PROCESS = "Saving..."
private const val SAVED_MESSAGE = "Successfully saved."
private val INCORRECT_FILE_TYPE = { extension: String -> "Couldn't save file with type '$extension'." }

class EditorViewModel(
    private val file: File,
    initialData: Array<Array<String?>>?,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val evaluator: Evaluator by inject(Evaluator::class.java)
    val tableModel: EvaluatingTableModel = EvaluatingTableModel(initialData, evaluator, scope)
    val editedFileName: String = file.path.substringAfterLast('/')

    fun onSaveClicked(onStatusUpdateListener: StatusUpdateListener) {
        onStatusUpdateListener.onStatusUpdated(SAVING_IN_PROCESS, Status.INFO)
        val extension = SupportedExtensions.fromFile(file) ?: run {
            onStatusUpdateListener.onStatusUpdated(INCORRECT_FILE_TYPE(file.extension), Status.ERROR)
            return
        }

        scope.launch {
            val saveResult = extension.fileHelper.writeTable(
                tableModel.getDataAsList(),
                file.path
            )

            val (result, status) = if (saveResult == null) {
                LastOpenFile.setPath(file.path)
                SAVED_MESSAGE to Status.INFO
            } else {
                saveResult to Status.ERROR
            }

            SwingUtilities.invokeLater {
                onStatusUpdateListener.onStatusUpdated(result, status)
            }
        }
    }
}

private fun EvaluatingTableModel.getDataAsList(): List<Pair<Int, Array<TableCell?>>> {
    val tableData = mutableListOf<Pair<Int, Array<TableCell?>>>()

    for (row in 0 until rowCount) {
        var hadNotNull = false
        val rowData = Array<TableCell?>(columnCount) { null }

        for (column in 1 until columnCount) {
            getValueAt(row, column)?.let {
                rowData[column] = it as? TableCell
                hadNotNull = true
            }
        }

        if (hadNotNull) {
            tableData.add(row to rowData)
        }
    }

    return tableData
}
