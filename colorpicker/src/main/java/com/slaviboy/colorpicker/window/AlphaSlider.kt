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
import com.slaviboy.colorpicker.component.drawCheckerboard
import com.slaviboy.colorpicker.model.ColorConverter
import com.slaviboy.colorpicker.model.toComposeColor
import com.slaviboy.colorpicker.state.ColorPickerState
import com.slaviboy.colorpicker.state.rememberColorPickerState
import com.slaviboy.colorpicker.util.offsetForValue
import com.slaviboy.colorpicker.util.valueForOffset
import kotlin.math.roundToInt

private const val ALPHA_LOWER = 0f
private const val ALPHA_UPPER = 255f

/**
 * Alpha slider (the original library's `SliderA`): a checkerboard background (to visualize
 * transparency) with a gradient from transparent (start) to the current hue color (end),
 * alpha [0,255] - unlike Hue/Value, this range is NOT inverted.
 */
@Composable
fun AlphaSlider(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
    style: SelectorStyle = SelectorStyle(),
    cornerRadius: Dp = 8.dp,
    orientation: Orientation = Orientation.Vertical,
    checkerSize: Dp = 6.dp,
) {
    LinearSelectorArea(
        modifier = modifier,
        style = style,
        cornerRadius = cornerRadius,
        orientation = orientation,
        selectorColor = ColorConverter.hsvToRgb(state.hue, 100, 100, state.alpha).toComposeColor(),
        selectorFraction = offsetForValue(state.alpha.toFloat(), ALPHA_LOWER, ALPHA_UPPER, 1f),
        onDrag = { fraction ->
            state.setAlpha(valueForOffset(fraction, ALPHA_LOWER, ALPHA_UPPER, 1f).roundToInt())
        },
        background = { bounds ->
            val checkerSizePx = checkerSize.toPx()
            drawCheckerboard(checkerSizePx, Color.LightGray, Color.White)
            drawAlphaGradient(bounds, orientation, state.baseHueColor)
        }
    )
}

private fun DrawScope.drawAlphaGradient(bounds: Rect, orientation: Orientation, hueColor: Color) {
    val brush = if (orientation == Orientation.Vertical) {
        Brush.verticalGradient(listOf(hueColor.copy(alpha = 0f), hueColor), startY = bounds.top, endY = bounds.bottom)
    } else {
        Brush.horizontalGradient(listOf(hueColor.copy(alpha = 0f), hueColor), startX = bounds.left, endX = bounds.right)
    }
    drawRect(brush = brush, topLeft = bounds.topLeft, size = bounds.size)
}

@Preview(showBackground = true)
@Composable
private fun AlphaSliderPreview() {
    AlphaSlider(
        state = rememberColorPickerState(Color(0xFF3F8CB5)),
        modifier = Modifier.size(28.dp, 220.dp)
    )
}
