package com.slaviboy.colorpicker.window

import androidx.compose.foundation.gestures.Orientation
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
import com.slaviboy.colorpicker.component.LinearSelectorArea
import com.slaviboy.colorpicker.component.SelectorStyle
import com.slaviboy.colorpicker.state.ColorPickerState
import com.slaviboy.colorpicker.state.rememberColorPickerState
import com.slaviboy.colorpicker.util.offsetForValue
import com.slaviboy.colorpicker.util.valueForOffset
import kotlin.math.roundToInt

private const val HUE_LOWER = 360f
private const val HUE_UPPER = 0f

/**
 * Hue slider (the original library's `SliderH`): a rainbow gradient along [orientation], hue
 * [360 at the start, 0 at the end] (an inverted range, matching the original).
 */
@Composable
fun HueSlider(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
    style: SelectorStyle = SelectorStyle(),
    cornerRadius: Dp = 8.dp,
    orientation: Orientation = Orientation.Vertical
) {
    // offsetForValue/valueForOffset are pixel<->value mappers; passing size=1f turns "pixels"
    // into "fraction of the track" directly, which is exactly what LinearSelectorArea expects.
    LinearSelectorArea(
        modifier = modifier,
        style = style,
        cornerRadius = cornerRadius,
        orientation = orientation,
        selectorColor = state.baseHueColor,
        selectorFraction = offsetForValue(state.hue.toFloat(), HUE_LOWER, HUE_UPPER, 1f),
        onDrag = { fraction ->
            state.setHue(valueForOffset(fraction, HUE_LOWER, HUE_UPPER, 1f).roundToInt())
        },
        background = { bounds -> drawHueGradient(bounds, orientation) }
    )
}

private val hueStopColors = listOf(
    Color(0xFFFF0000), Color(0xFFFF00FF), Color(0xFF0000FF),
    Color(0xFF00FFFF), Color(0xFF00FF00), Color(0xFFFFFF00), Color(0xFFFF0000)
)
private val hueStopPositions = listOf(0.00f, 0.166f, 0.33f, 0.5f, 0.66f, 0.83f, 1f)

private fun DrawScope.drawHueGradient(bounds: Rect, orientation: Orientation) {
    val stops = hueStopPositions.zip(hueStopColors).toTypedArray()
    val brush = if (orientation == Orientation.Vertical) {
        Brush.verticalGradient(colorStops = stops, startY = bounds.top, endY = bounds.bottom)
    } else {
        Brush.horizontalGradient(colorStops = stops, startX = bounds.left, endX = bounds.right)
    }
    drawRect(brush = brush, topLeft = bounds.topLeft, size = bounds.size)
}

@Preview(showBackground = true)
@Composable
private fun HueSliderPreview() {
    HueSlider(
        state = rememberColorPickerState(Color(0xFF3F8CB5)),
        modifier = Modifier.size(28.dp, 220.dp)
    )
}
