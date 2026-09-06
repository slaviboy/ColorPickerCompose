package com.slaviboy.colorpicker.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
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
 * A 1D gesture + drawing surface: the selector follows a single axis (chosen by [orientation]),
 * driven by a fraction [0,1] (0 = top/left, 1 = bottom/right); it stays centered on the
 * cross-axis, mimicking a slider. Replaces the original library's `Slider` component; knows
 * nothing about [com.slaviboy.colorpicker.state.ColorPickerState] - callers translate their own
 * semantic fields to/from the fraction.
 *
 * Padding is intentionally asymmetric, matching the original library's `Slider.onInit`: the full
 * selector [SelectorStyle.contentPadding] is only reserved along the main (drag) axis, so the
 * selector never clips past the ends; the cross axis only reserves the thin border width, so the
 * track fills nearly the whole width/height and the round selector knob is allowed to overhang
 * it slightly, like a normal slider thumb - reserving the full selector padding on the cross axis
 * too would collapse a narrow track (e.g. width == selector diameter) to zero size.
 */
@Composable
fun LinearSelectorArea(
    selectorColor: Color,
    selectorFraction: Float,
    onDrag: (fraction: Float) -> Unit,
    background: DrawScope.(bounds: Rect) -> Unit,
    modifier: Modifier = Modifier,
    style: SelectorStyle = SelectorStyle(),
    cornerRadius: Dp = 0.dp,
    orientation: Orientation = Orientation.Vertical,
) {
    val density = LocalDensity.current
    val mainAxisPaddingPx = with(density) { style.contentPadding().toPx() }
    val crossAxisPaddingPx = with(density) { style.borderWidth.toPx() }
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }

    fun bounds(width: Float, height: Float): Rect = if (orientation == Orientation.Vertical) {
        Rect(crossAxisPaddingPx, mainAxisPaddingPx, width - crossAxisPaddingPx, height - mainAxisPaddingPx)
    } else {
        Rect(mainAxisPaddingPx, crossAxisPaddingPx, width - mainAxisPaddingPx, height - crossAxisPaddingPx)
    }

    Canvas(
        modifier = modifier.dragToMove { position, size ->
            val bounds = bounds(size.width.toFloat(), size.height.toFloat())
            val fraction = if (orientation == Orientation.Vertical) {
                if (bounds.height <= 0f) 0f else (position.y.coerceIn(bounds.top, bounds.bottom) - bounds.top) / bounds.height
            } else {
                if (bounds.width <= 0f) 0f else (position.x.coerceIn(bounds.left, bounds.right) - bounds.left) / bounds.width
            }
            onDrag(fraction)
        }
    ) {
        val bounds = bounds(size.width, size.height)
        val cr = CornerRadius(cornerRadiusPx, cornerRadiusPx)

        val clip = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(bounds, cr, cr, cr, cr)) }
        clipPath(clip) { background(bounds) }

        drawRoundRectBorder(bounds, cr, style)

        val selectorCenter = if (orientation == Orientation.Vertical) {
            Offset(size.width / 2f, bounds.top + selectorFraction * bounds.height)
        } else {
            Offset(bounds.left + selectorFraction * bounds.width, size.height / 2f)
        }
        drawSelector(selectorCenter, selectorColor, style)
    }
}
