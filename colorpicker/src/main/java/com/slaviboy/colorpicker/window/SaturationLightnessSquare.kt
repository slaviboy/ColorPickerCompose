package com.slaviboy.colorpicker.window

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slaviboy.colorpicker.component.RectangularSelectorArea
import com.slaviboy.colorpicker.component.SelectorStyle
import com.slaviboy.colorpicker.model.ColorConverter
import com.slaviboy.colorpicker.model.toComposeColor
import com.slaviboy.colorpicker.state.ColorPickerState
import com.slaviboy.colorpicker.state.rememberColorPickerState
import kotlin.math.roundToInt

/**
 * Saturation/Lightness square (the original library's `RectangularSL`): horizontal = saturation
 * [100 at left, 0 at right], vertical = lightness [100 at top, 0 at bottom].
 *
 * Background is 2 sequential, normally-composited gradient draws. Both `s` and `l` enter
 * HSL->RGB affinely for a fixed hue, so the true color surface is exactly bilinear in (s,l) -
 * this 2-layer decomposition reproduces it exactly (verified algebraically): a horizontal
 * gradient from the pure hue at L50 (s=100) to **mid-gray, not white** (s=0 - using flat white
 * here would be visibly wrong at low saturation), then a vertical white/transparent/black
 * gradient on top (transparent at the L50 midpoint reveals the horizontal layer unmodified,
 * exactly matching the true surface at l=50 for every s).
 */
@Composable
fun SaturationLightnessSquare(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
    style: SelectorStyle = SelectorStyle(),
    cornerRadius: Dp = 8.dp,
) {
    RectangularSelectorArea(
        modifier = modifier.aspectRatio(1f),
        style = style,
        cornerRadius = cornerRadius,
        selectorColor = state.color,
        selectorXFraction = 1f - state.hslSaturation / 100f,
        selectorYFraction = 1f - state.lightness / 100f,
        onDrag = { xFraction, yFraction ->
            state.setSaturationLightness((1f - xFraction).let { (it * 100f).roundToInt() }, ((1f - yFraction) * 100f).roundToInt())
        },
        background = { bounds ->
            val hueAtL50 = ColorConverter.hslToRgb(state.hue, 100, 50).toComposeColor()
            drawSaturationLightnessBackground(bounds, hueAtL50)
        }
    )
}

private fun DrawScope.drawSaturationLightnessBackground(bounds: Rect, hueAtL50: Color) {
    val midGray = Color(0.5f, 0.5f, 0.5f)
    drawRect(
        brush = Brush.horizontalGradient(listOf(hueAtL50, midGray), startX = bounds.left, endX = bounds.right),
        topLeft = bounds.topLeft,
        size = bounds.size
    )
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White, Color.Transparent, Color.Black),
            startY = bounds.top,
            endY = bounds.bottom
        ),
        topLeft = bounds.topLeft,
        size = bounds.size
    )
}

@Preview(showBackground = true)
@Composable
private fun SaturationLightnessSquarePreview() {
    SaturationLightnessSquare(
        state = rememberColorPickerState(Color(0xFF3F8CB5)),
        modifier = Modifier.size(220.dp)
    )
}
