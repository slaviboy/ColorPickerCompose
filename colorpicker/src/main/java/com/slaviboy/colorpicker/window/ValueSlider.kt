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
import com.slaviboy.colorpicker.model.ColorConverter
import com.slaviboy.colorpicker.model.toComposeColor
import com.slaviboy.colorpicker.state.ColorPickerState
import com.slaviboy.colorpicker.state.rememberColorPickerState
import com.slaviboy.colorpicker.util.offsetForValue
import com.slaviboy.colorpicker.util.valueForOffset
import kotlin.math.roundToInt

private const val VALUE_LOWER = 100f
private const val VALUE_UPPER = 0f

/**
 * Value slider (the original library's `SliderV`): gradient from the current hue color (start)
 * to black (end), value [100 at the start, 0 at the end]. Its gradient recomputes automatically
 * whenever hue changes elsewhere (e.g. dragging the wheel or hue slider) - in Compose "redraw
 * only" from the original's propagation table is just "this composable reads `state.hue`".
 */
@Composable
fun ValueSlider(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
    style: SelectorStyle = SelectorStyle(),
    cornerRadius: Dp = 8.dp,
    orientation: Orientation = Orientation.Vertical,
) {
    LinearSelectorArea(
        modifier = modifier,
        style = style,
        cornerRadius = cornerRadius,
        orientation = orientation,
        selectorColor = ColorConverter.hsvToRgb(state.hue, 100, state.value).toComposeColor(),
        selectorFraction = offsetForValue(state.value.toFloat(), VALUE_LOWER, VALUE_UPPER, 1f),
        onDrag = { fraction ->
            state.setValue(valueForOffset(fraction, VALUE_LOWER, VALUE_UPPER, 1f).roundToInt())
        },
        background = { bounds -> drawValueGradient(bounds, orientation, state.baseHueColor) }
    )
}

private fun DrawScope.drawValueGradient(bounds: Rect, orientation: Orientation, hueColor: Color) {
    val brush = if (orientation == Orientation.Vertical) {
        Brush.verticalGradient(listOf(hueColor, Color.Black), startY = bounds.top, endY = bounds.bottom)
    } else {
        Brush.horizontalGradient(listOf(hueColor, Color.Black), startX = bounds.left, endX = bounds.right)
    }
    drawRect(brush = brush, topLeft = bounds.topLeft, size = bounds.size)
}

@Preview(showBackground = true)
@Composable
private fun ValueSliderPreview() {
    ValueSlider(
        state = rememberColorPickerState(Color(0xFF3F8CB5)),
        modifier = Modifier.size(28.dp, 220.dp)
    )
}
