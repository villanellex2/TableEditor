package com.echernikova.file

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.editor.table.model.TableCell
import com.echernikova.evaluator.core.ErrorEvaluationResult
import com.echernikova.evaluator.core.EvaluationResult
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.io.FileOutputStream

class XSSFileHelper : FileHelper {
    override fun writeTable(
        table: List<Array<String?>>,
        mapEvaluated: Map<CellPointer, EvaluationResult<*>>,
        filePath: String
    ): String? {
        val workbook: Workbook = XSSFWorkbook()
        val sheet: Sheet = workbook.createSheet("Table Data")

        runCatching {
            if (table.isNotEmpty()) {
                val columnCount = table[0].size

                for (row in table.indices) {
                    val rowIndex = table[row][0]?.toInt() ?: row
                    val excelRow = sheet.createRow(rowIndex)

                    for (column in 1 until columnCount) {
                        table[row][column]?.let { value ->
                            val cell = excelRow.createCell(column - 1)

                            val pointer = CellPointer(row, column)
                            val result = mapEvaluated.get(pointer)

                            if (result is ErrorEvaluationResult) {
                                workbook.close()
                                return "Error on saving cell '$pointer'. ${result.evaluatedValue}"
                            }

                            cell.fillValue(value, result)
                        }
                    }
                }
            }

            FileOutputStream(filePath).use { fileOut -> workbook.write(fileOut) }

        }.onFailure { exception ->
            workbook.close()
            return exception.localizedMessage
        }

        println("File successfully saved $filePath")
        return null
    }

    override fun readTable(filePath: String): List<Array<Any?>>? {
        val fileInputStream = runCatching {
            FileInputStream(filePath)
        }.getOrNull() ?: return null

        val workbook: Workbook = runCatching {
            XSSFWorkbook(fileInputStream)
        }.getOrNull() ?: run {
            fileInputStream.close()
            return null
        }

        val sheet: Sheet = workbook.getSheetAt(0)
        val data: MutableList<Array<Any?>> = mutableListOf()

        var currentRow = 0
        for (row in sheet) {
            while (row.rowNum > currentRow) {
                data.add(arrayOf(currentRow.toString()))
                currentRow++
            }

            var currentColumn = 0
            val rowData = mutableListOf<String?>()
            rowData.add(row.rowNum.toString())

            for (cell in row) {
                while (cell.columnIndex > currentColumn) {
                    rowData.add(null)
                    currentColumn++
                }
                val cellValue = cell.getValue()
                currentColumn++
                rowData.add(cellValue)
            }
            currentRow++
            data.add(rowData.toTypedArray())
        }

        workbook.close()
        fileInputStream.close()

        return data
    }

    private fun Cell.getValue(): String {
        return when (cellType) {
            CellType.STRING -> stringCellValue
            CellType.NUMERIC -> if (DateUtil.isCellDateFormatted(this)) {
                dateCellValue.toString()
            } else {
                numericCellValue.toString()
            }

            CellType.BOOLEAN -> booleanCellValue.toString()
            CellType.FORMULA -> "=$cellFormula"
            else -> ""
        }
    }

    private fun Cell.fillValue(rawValue: String, evaluationResult: EvaluationResult<*>?) {
        if (evaluationResult?.evaluatedValue == null) {
            setCellValue(rawValue)
            return
        }

        if (rawValue.startsWith("=")) {
            runCatching {
                cellFormula = rawValue.substring(1, rawValue.length)
            }.onFailure {
                setCellValue(rawValue)
            }
        }

        when (val value = evaluationResult.evaluatedValue) {
            is Double -> setCellValue(value)
            is String -> setCellValue(value)
            else -> setCellValue(rawValue)
        }

        return
    }
}