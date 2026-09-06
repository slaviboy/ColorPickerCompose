package com.slaviboy.colorpicker.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slaviboy.colorpicker.component.SelectorStyle
import com.slaviboy.colorpicker.state.ColorPickerState
import com.slaviboy.colorpicker.state.rememberColorPickerState
import com.slaviboy.colorpicker.textfield.ColorFormat
import com.slaviboy.colorpicker.textfield.ColorValueTextField
import com.slaviboy.colorpicker.window.AlphaSlider
import com.slaviboy.colorpicker.window.HueSlider
import com.slaviboy.colorpicker.window.SaturationValueSquare

/**
 * Full HSV color picker (the original library's `RectangularHSV`): a Saturation/Value square,
 * a Hue slider, an Alpha slider, and an optional value text field, laid out in a row.
 */
@Composable
fun RectangularHsvColorPicker(
    state: ColorPickerState = rememberColorPickerState(),
    modifier: Modifier = Modifier,
    mainSize: Dp = 220.dp,
    sliderWidth: Dp = 28.dp,
    spacing: Dp = 12.dp,
    style: SelectorStyle = SelectorStyle(),
    valueFormat: ColorFormat = ColorFormat.Hsv,
    showTextField: Boolean = true,
) {
    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            SaturationValueSquare(state, Modifier.size(mainSize), style)
            HueSlider(state, Modifier.size(sliderWidth, mainSize), style)
            AlphaSlider(state, Modifier.size(sliderWidth, mainSize), style)
        }
        if (showTextField) {
            ColorValueTextField(state, valueFormat, Modifier.fillMaxWidth().padding(top = spacing))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RectangularHsvColorPickerPreview() {
    RectangularHsvColorPicker(state = rememberColorPickerState(Color(0xFF3F8CB5)))
}
