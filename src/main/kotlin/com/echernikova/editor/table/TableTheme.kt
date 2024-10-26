package com.echernikova.editor.table

import com.echernikova.utils.parseAsARGB
import java.awt.Color
import java.awt.Font

object TableTheme {
    val gridColor = "#1e000000".parseAsARGB()
    val statusBarTextSize = 12

    val normalCellFont = Font("SansSerif", Font.PLAIN, 12)
    val normalCellColor = Color.BLACK

    val formulaCellFont = Font("Monospaced", Font.PLAIN, 12)
    val formulaCellColor = "#ff036280".parseAsARGB()

    val errorCellFont = Font("Monospaced", Font.PLAIN, 12)
    val errorCellColor = "#ffff0000".parseAsARGB()
}
