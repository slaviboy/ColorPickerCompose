package com.slaviboy.colorpicker.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A rectangular 2D gesture + drawing surface: the selector follows the full plane, driven by an
 * x fraction and a y fraction, each [0,1] (0 = left/top, 1 = right/bottom). Replaces the original
 * library's `Rectangular` component; knows nothing about
 * [com.slaviboy.colorpicker.state.ColorPickerState] - callers translate their own semantic fields
 * to/from x/y fractions.
 */
@Composable
fun RectangularSelectorArea(
    selectorColor: Color,
    selectorXFraction: Float,
    selectorYFraction: Float,
    onDrag: (xFraction: Float, yFraction: Float) -> Unit,
    background: DrawScope.(bounds: Rect) -> Unit,
    modifier: Modifier = Modifier,
    style: SelectorStyle = SelectorStyle(),
    cornerRadius: Dp = 0.dp
) {
    val density = LocalDensity.current
    val paddingPx = with(density) { style.contentPadding().toPx() }
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }

    Canvas(
        modifier = modifier.dragToMove { position, size ->
            val bounds = Rect(paddingPx, paddingPx, size.width - paddingPx, size.height - paddingPx)
            // Guards a square smaller than its own required padding (e.g. mid-recomposition
            // before layout settles) from dividing by a zero/negative width or height below.
            if (bounds.width <= 0f || bounds.height <= 0f) return@dragToMove
            val x = position.x.coerceIn(bounds.left, bounds.right)
            val y = position.y.coerceIn(bounds.top, bounds.bottom)
            onDrag((x - bounds.left) / bounds.width, (y - bounds.top) / bounds.height)
        }
    ) {
        val bounds = Rect(paddingPx, paddingPx, size.width - paddingPx, size.height - paddingPx)
        val cr = CornerRadius(cornerRadiusPx, cornerRadiusPx)

        val clip = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(bounds, cr, cr, cr, cr)) }
        clipPath(clip) { background(bounds) }

        drawRoundRectBorder(bounds, cr, style)

        val selectorCenter = Offset(
            bounds.left + selectorXFraction * bounds.width,
            bounds.top + selectorYFraction * bounds.height
        )
        drawSelector(selectorCenter, selectorColor, style)
    }
}
