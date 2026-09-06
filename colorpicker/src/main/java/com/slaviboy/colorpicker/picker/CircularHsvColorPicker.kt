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
import com.slaviboy.colorpicker.window.HueSaturationWheel
import com.slaviboy.colorpicker.window.ValueSlider

/**
 * Full HSV color picker (the original library's `CircularHSV`): a Hue/Saturation wheel, a Value
 * slider, an Alpha slider, and an optional value text field, laid out in a row.
 */
@Composable
fun CircularHsvColorPicker(
    modifier: Modifier = Modifier,
    state: ColorPickerState = rememberColorPickerState(),
    mainSize: Dp = 220.dp,
    sliderWidth: Dp = 36.dp,
    spacing: Dp = 12.dp,
    style: SelectorStyle = SelectorStyle(),
    valueFormat: ColorFormat = ColorFormat.Hsv,
    showTextField: Boolean = false
) {
    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            HueSaturationWheel(state, Modifier.size(mainSize), style)
            ValueSlider(state, Modifier.size(sliderWidth, mainSize), style)
            AlphaSlider(state, Modifier.size(sliderWidth, mainSize), style)
        }
        if (showTextField) {
            ColorValueTextField(state, valueFormat, Modifier.fillMaxWidth().padding(top = spacing))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CircularHsvColorPickerPreview() {
    CircularHsvColorPicker(state = rememberColorPickerState(Color(0xFF3F8CB5)))
}
