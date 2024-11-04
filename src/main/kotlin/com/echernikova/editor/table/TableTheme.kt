package com.echernikova.editor.table

import java.awt.Color
import java.awt.Font

object TableTheme {
    val gridColor = Color(0xCDD1D6)
    val statusBarTextSize = 14

    val normalCellFont = Font("SansSerif", Font.PLAIN, 14)
    val normalCellColor = Color.BLACK

    val formulaCellFont = Font("Monospaced", Font.PLAIN, 14)
    val formulaCellColor = Color(0x036280)

    val errorCellFont = Font("Monospaced", Font.PLAIN, 14)
    val errorCellColor = Color(0xff0000)

    val tableSelectionBackgroundColor = Color(0xE5EBFF)
    val tableSelectionForegroundColor = Color(0x333333)
}
