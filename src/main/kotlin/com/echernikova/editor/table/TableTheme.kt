package com.echernikova.editor.table

import java.awt.Color
import java.awt.Font

interface TableTheme {
    val gridColor: Color
    val primaryBackground: Color
    val secondaryBackground: Color

    val statusBarTextSize: Int

    val normalCellFont: Font
    val normalCellFontColor: Color

    val markersCellFont: Font
    val markersCellFontColor: Color

    val errorCellFont: Font
    val errorCellFontColor: Color

    val tableSelectionBackgroundColor: Color
    val tableSelectionFontColor: Color

    val tableEditorBackgroundColor: Color
    val tableEditorFontColor: Color

    companion object {
        var currentTheme: TableTheme = LightTableTheme()
    }
}

class LightTableTheme: TableTheme {
    override val gridColor = Color(0xCDD1D6)
    override val primaryBackground = Color(0xFFFFFF)
    override val secondaryBackground = Color(0xF5F5F5)

    override val statusBarTextSize = 14

    override val normalCellFont = Font(Font.SANS_SERIF, Font.PLAIN, 14)
    override val normalCellFontColor = Color.BLACK

    override val markersCellFont = Font(Font.MONOSPACED, Font.PLAIN, 13)
    override val markersCellFontColor = Color(0x242E47)

    override val errorCellFont = Font(Font.MONOSPACED, Font.PLAIN, 14)
    override val errorCellFontColor = Color(0xff0000)

    override val tableSelectionBackgroundColor = Color(0xE5EBFF)
    override val tableSelectionFontColor = Color(0x333333)

    override val tableEditorBackgroundColor = Color(0xCDD9FF)
    override val tableEditorFontColor = Color(0x1E1E1E)
}
