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
import com.slaviboy.colorpicker.state.ColorPickerState
import com.slaviboy.colorpicker.state.rememberColorPickerState
import kotlin.math.roundToInt

/**
 * Saturation/Value square (the original library's `RectangularSV`): horizontal = saturation
 * [0,100], vertical = value [100 at top, 0 at bottom].
 *
 * Background is 2 sequential, normally-composited gradient draws - verified algebraically equal
 * to the exact HSV->RGB formula (`color = value * lerp(white, hueColor, saturation)`): a
 * horizontal white-to-hue gradient, then a vertical transparent-to-black gradient on top.
 */
@Composable
fun SaturationValueSquare(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
    style: SelectorStyle = SelectorStyle(),
    cornerRadius: Dp = 8.dp
) {
    RectangularSelectorArea(
        modifier = modifier.aspectRatio(1f),
        style = style,
        cornerRadius = cornerRadius,
        selectorColor = state.color,
        selectorXFraction = state.hsvSaturation / 100f,
        selectorYFraction = 1f - state.value / 100f,
        onDrag = { xFraction, yFraction ->
            state.setSaturationValue((xFraction * 100f).roundToInt(), ((1f - yFraction) * 100f).roundToInt())
        },
        background = { bounds -> drawSaturationValueBackground(bounds, state.baseHueColor) }
    )
}

private fun DrawScope.drawSaturationValueBackground(bounds: Rect, hueColor: Color) {
    drawRect(
        brush = Brush.horizontalGradient(listOf(Color.White, hueColor), startX = bounds.left, endX = bounds.right),
        topLeft = bounds.topLeft,
        size = bounds.size
    )
    drawRect(
        brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black), startY = bounds.top, endY = bounds.bottom),
        topLeft = bounds.topLeft,
        size = bounds.size
    )
}

@Preview(showBackground = true)
@Composable
private fun SaturationValueSquarePreview() {
    SaturationValueSquare(
        state = rememberColorPickerState(Color(0xFF3F8CB5)),
        modifier = Modifier.size(220.dp)
    )
}
