package com.echernikova.file

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.swing.JTable

class XSSFileHelper: FileHelper {
    override fun writeTable(table: List<Array<String?>>, filePath: String): Throwable? {
        val workbook: Workbook = XSSFWorkbook()
        val sheet: Sheet = workbook.createSheet("Table Data")

        if (table.isNotEmpty()) {
            val columnCount = table[0].size

            for (row in table.indices) {
                val rowIndex = table[row][0]?.toInt() ?: row
                val excelRow = sheet.createRow(rowIndex)

                for (i in 1 until columnCount) {
                    table[row][i]?.let { column ->
                        val cell = excelRow.createCell(i - 1)
                        cell.setCellValue(column)
                    }
                }
            }
        }

        runCatching {
            FileOutputStream(filePath).use { fileOut ->
                workbook.write(fileOut)
            }
        }.onFailure { exception ->
            workbook.close()
            return exception
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
                val cellValue = when (cell.cellType) {
                    CellType.STRING -> cell.stringCellValue
                    CellType.NUMERIC -> if (DateUtil.isCellDateFormatted(cell)) {
                        cell.dateCellValue.toString()
                    } else {
                        cell.numericCellValue.toString()
                    }

                    CellType.BOOLEAN -> cell.booleanCellValue.toString()
                    CellType.FORMULA -> cell.cellFormula
                    else -> ""
                }
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
}