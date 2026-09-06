package com.slaviboy.colorpicker.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import com.slaviboy.colorpicker.util.angleBetweenTwoPoints
import com.slaviboy.colorpicker.util.offsetForAngleDistance
import kotlin.math.hypot

/**
 * A circular 2D gesture + drawing surface: the selector follows a circular path, driven by an
 * angle [0,360) and a distance fraction [0,1] of the circle's radius. Replaces the original
 * library's `Circular` component; knows nothing about [com.slaviboy.colorpicker.state.ColorPickerState] -
 * callers translate their own semantic fields to/from angle+distance.
 */
@Composable
fun CircularSelectorArea(
    modifier: Modifier = Modifier,
    style: SelectorStyle = SelectorStyle(),
    selectorColor: Color,
    selectorAngleDeg: Float,
    selectorDistanceFraction: Float,
    onDrag: (angleDeg: Float, distanceFraction: Float) -> Unit,
    background: DrawScope.(center: Offset, radiusPx: Float) -> Unit,
) {
    val density = LocalDensity.current
    val paddingPx = with(density) { style.contentPadding().toPx() }

    Canvas(
        modifier = modifier.dragToMove { position, size ->
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (minOf(size.width, size.height) / 2f - paddingPx).coerceAtLeast(0f)
            val dx = position.x - center.x
            val dy = position.y - center.y
            val angle = angleBetweenTwoPoints(dx, dy)
            // Clamping the polar distance to the radius keeps a touch outside the wheel on its
            // rim, at the correct angle - equivalent to the original library's Cartesian
            // lerp-onto-edge, just simpler in polar coordinates.
            val distance = hypot(dx, dy).coerceAtMost(radius)
            onDrag(angle, if (radius > 0f) distance / radius else 0f)
        }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (minOf(size.width, size.height) / 2f - paddingPx).coerceAtLeast(0f)

        val clip = Path().apply { addOval(androidx.compose.ui.geometry.Rect(center = center, radius = radius)) }
        clipPath(clip) { background(center, radius) }

        drawCircularBorder(radius, center, style)

        val selectorCenter = offsetForAngleDistance(center, selectorAngleDeg, selectorDistanceFraction * radius)
        drawSelector(selectorCenter, selectorColor, style)
    }
}
