package com.echernikova.editor.table

import java.awt.Color
import java.awt.Font

interface TableTheme {
    val gridColor: Color
    val statusBarTextSize: Int

    val normalCellFont: Font
    val normalCellColor: Color?

    val formulaCellFont: Font
    val formulaCellColor: Color

    val errorCellFont: Font
    val errorCellColor: Color

    val tableSelectionBackgroundColor: Color
    val tableSelectionForegroundColor: Color

    companion object {
        var currentTheme: TableTheme = LightTableTheme()
    }
}

class LightTableTheme: TableTheme {
    override val gridColor = Color(0xCDD1D6)
    override val statusBarTextSize = 14

    override val normalCellFont = Font("SansSerif", Font.PLAIN, 14)
    override val normalCellColor = Color.BLACK

    override val formulaCellFont = Font("Monospaced", Font.PLAIN, 14)
    override val formulaCellColor = Color(0x036280)

    override val errorCellFont = Font("Monospaced", Font.PLAIN, 14)
    override val errorCellColor = Color(0xff0000)

    override val tableSelectionBackgroundColor = Color(0xE5EBFF)
    override val tableSelectionForegroundColor = Color(0x333333)
}
