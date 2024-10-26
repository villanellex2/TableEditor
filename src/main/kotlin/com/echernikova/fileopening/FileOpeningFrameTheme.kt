package com.echernikova.fileopening

import java.awt.Color

interface FileOpeningFrameTheme {
    val buttonsFontSize: Float
        get() = 16f
    val buttonFontColor: Color

    val createButtonBackgroundColor: Color
    val openButtonBackgroundColor: Color
    val openLastButtonBackgroundColorActive: Color
    val openLastButtonBackgroundColorInactive: Color

    companion object {
        val currentTheme = LightFileOpeningFrameTheme()
    }
}

class LightFileOpeningFrameTheme: FileOpeningFrameTheme {
    override val buttonFontColor = Color(0x1e1010)

    override val createButtonBackgroundColor = Color(0xceceff)
    override val openButtonBackgroundColor = Color(0xf2d8f5)
    override val openLastButtonBackgroundColorActive = Color(0xcee6ff)
    override val openLastButtonBackgroundColorInactive = Color(0xe3e6ea)
}
