package com.echernikova.utils

import java.awt.Color

/**
 * Parse ARGB String to color.
 */
fun String.parseAsARGB(): Color {
    val cleanString = removePrefix("#")

    val argb = cleanString.toLong(16).toInt()

    val alpha = (argb shr 24) and 0xFF
    val red = (argb shr 16) and 0xFF
    val green = (argb shr 8) and 0xFF
    val blue = argb and 0xFF

    return Color(red, green, blue, alpha)
}

