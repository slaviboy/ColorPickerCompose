package com.slaviboy.colorpicker.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Draws an alternating checkerboard pattern, used behind the alpha slider to visualize
 * transparency (the original library's "zebra" background).
 */
fun DrawScope.drawCheckerboard(cellSize: Float, colorA: Color, colorB: Color) {
    if (cellSize <= 0f) return
    drawRect(colorB, size = size)
    var row = 0
    var y = 0f
    while (y < size.height) {
        var col = 0
        var x = if (row % 2 == 0) 0f else cellSize
        while (x < size.width) {
            drawRect(colorA, topLeft = Offset(x, y), size = Size(cellSize, cellSize))
            x += cellSize * 2
            col++
        }
        y += cellSize
        row++
    }
}
