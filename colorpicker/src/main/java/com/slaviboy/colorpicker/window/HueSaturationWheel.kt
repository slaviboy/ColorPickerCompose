package com.slaviboy.colorpicker.window

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slaviboy.colorpicker.component.CircularSelectorArea
import com.slaviboy.colorpicker.component.SelectorStyle
import com.slaviboy.colorpicker.model.ColorConverter
import com.slaviboy.colorpicker.model.toComposeColor
import com.slaviboy.colorpicker.state.ColorPickerState
import com.slaviboy.colorpicker.state.rememberColorPickerState
import kotlin.math.roundToInt

/**
 * Hue/Saturation color wheel (the original library's `CircularHS`): angle around the wheel
 * selects hue, distance from the center selects saturation. A flat black overlay dims the whole
 * wheel to represent the current V(alue), since a 2D wheel can only encode two of HSV's three
 * channels directly.
 *
 * The background is 3 sequential, normally-composited gradient draws - verified algebraically
 * equal to the exact HSV->RGB formula (`color = value * lerp(white, hueColor, saturation)`):
 * a sweep gradient for hue (replacing the original View-based library's 3600-line-per-frame
 * per-degree workaround - Compose has a native sweep gradient primitive the old Canvas API
 * lacked), a white-to-transparent radial gradient for desaturation toward the center, and the
 * flat black dimming overlay for V.
 */
@Composable
fun HueSaturationWheel(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
    style: SelectorStyle = SelectorStyle(),
) {
    CircularSelectorArea(
        modifier = modifier.aspectRatio(1f),
        style = style,
        selectorColor = state.color,
        selectorAngleDeg = state.hue.toFloat(),
        selectorDistanceFraction = state.hsvSaturation / 100f,
        onDrag = { angleDeg, distanceFraction ->
            state.setHueSaturation(angleDeg.roundToInt(), (distanceFraction * 100f).roundToInt())
        },
        background = { center, radiusPx -> drawHueSaturationWheelBackground(center, radiusPx, state.value) }
    )
}

private fun DrawScope.drawHueSaturationWheelBackground(center: Offset, radiusPx: Float, value: Int) {
    val hueStops = (0..360).map { degree -> ColorConverter.hsvToRgb(degree % 360, 100, 100).toComposeColor() }

    drawCircle(
        brush = Brush.sweepGradient(colors = hueStops, center = center),
        radius = radiusPx,
        center = center
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color.Transparent),
            center = center,
            radius = radiusPx
        ),
        radius = radiusPx,
        center = center
    )
    drawCircle(
        color = Color.Black.copy(alpha = 1f - value / 100f),
        radius = radiusPx,
        center = center
    )
}

@Preview(showBackground = true)
@Composable
private fun HueSaturationWheelPreview() {
    HueSaturationWheel(
        state = rememberColorPickerState(Color(0xFF3F8CB5)),
        modifier = Modifier.size(220.dp)
    )
}
