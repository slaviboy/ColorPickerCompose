package com.slaviboy.colorpicker.component

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Draws a color window's selector: an extra stroke ring (for contrast against any background
 * color), a filled preview disc, then the main stroke ring - same layering order as the original
 * library's `Base.drawSelector`.
 */
fun DrawScope.drawSelector(center: Offset, fillColor: Color, style: SelectorStyle) {
    val radiusPx = style.radius.toPx()
    val strokeWidthPx = style.strokeWidth.toPx()
    val extraStrokeWidthPx = style.extraStrokeWidth.toPx()

    if (extraStrokeWidthPx > 0f) {
        val halfStroke = strokeWidthPx / 2f
        val extraRadius = radiusPx + halfStroke + extraStrokeWidthPx / 2f
        val innerExtraRadius = radiusPx - halfStroke - extraStrokeWidthPx / 2f
        drawCircle(style.extraStrokeColor, radius = extraRadius, center = center, style = Stroke(extraStrokeWidthPx))
        if (innerExtraRadius > 0f) {
            drawCircle(style.extraStrokeColor, radius = innerExtraRadius, center = center, style = Stroke(extraStrokeWidthPx))
        }
    }

    drawCircle(fillColor, radius = radiusPx, center = center)

    if (strokeWidthPx > 0f) {
        drawCircle(style.strokeColor, radius = radiusPx, center = center, style = Stroke(strokeWidthPx))
    }
}

/** Draws the border stroke around a circular color window (e.g. the hue/saturation wheel). */
fun DrawScope.drawCircularBorder(radiusPx: Float, center: Offset, style: SelectorStyle) {
    val widthPx = style.borderWidth.toPx()
    if (widthPx <= 0f) return
    drawCircle(style.borderColor, radius = radiusPx, center = center, style = Stroke(widthPx))
}

/** Draws the border stroke around a rounded-rect color window (square/slider). */
fun DrawScope.drawRoundRectBorder(bounds: Rect, cornerRadius: CornerRadius, style: SelectorStyle) {
    val widthPx = style.borderWidth.toPx()
    if (widthPx <= 0f) return
    drawRoundRect(
        color = style.borderColor,
        topLeft = bounds.topLeft,
        size = bounds.size,
        cornerRadius = cornerRadius,
        style = Stroke(widthPx)
    )
}
