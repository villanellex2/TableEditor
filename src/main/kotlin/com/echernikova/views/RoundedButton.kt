package com.echernikova.views

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JButton
import javax.swing.border.EmptyBorder

class RoundedButton(
    text: String,
    private val cornersRadius: Int
) : JButton(text) {
    init {
        isContentAreaFilled = false
        border = EmptyBorder(cornersRadius, cornersRadius, cornersRadius, cornersRadius)
    }

    override fun paintComponent(graphics: Graphics) {
        val g2 = graphics as? Graphics2D ?: run {
            super.paintComponent(graphics)
            return
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = background
        g2.fillRoundRect(0, 0, width, height, cornersRadius, cornersRadius)

        super.paintComponent(graphics)
    }

    override fun paintBorder(graphics: Graphics?) {
        val g2 = graphics as? Graphics2D ?: run {
            super.paintBorder(graphics)
            return
        }

        g2.drawRoundRect(0, 0, width - 1, height - 1, cornersRadius, cornersRadius)
    }
}
