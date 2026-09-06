package com.slaviboy.colorpickercompose.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.slaviboy.colorpicker.picker.CircularHsvColorPicker
import com.slaviboy.colorpicker.picker.RectangularHslColorPicker
import com.slaviboy.colorpicker.picker.RectangularHsvColorPicker
import com.slaviboy.colorpicker.state.rememberColorPickerState
import com.slaviboy.colorpicker.textfield.ColorFormat
import com.slaviboy.colorpicker.textfield.ColorValueTextField
import kotlin.random.Random

/**
 * Demo screen reproducing the original library's sample app: all three composed pickers sharing
 * one [com.slaviboy.colorpicker.state.ColorPickerState] (so dragging any one picker visibly moves
 * the selectors of the other two), a format-aware value field, buttons to switch that field's
 * format, and a "Random Color" button.
 */
@Composable
fun ColorPickerDemoScreen(modifier: Modifier = Modifier) {
    val state = rememberColorPickerState(initial = Color(0xFF3F8CB5))
    var selectedFormat by remember { mutableStateOf<ColorFormat>(ColorFormat.Hsv) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PickerSection(title = "RectangularHSV") {
            RectangularHsvColorPicker(state = state, showTextField = false)
        }
        PickerSection(title = "RectangularHSL") {
            RectangularHslColorPicker(state = state, showTextField = false)
        }
        PickerSection(title = "CircularHSV") {
            CircularHsvColorPicker(state = state, showTextField = false)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = selectedFormat.label(), style = MaterialTheme.typography.labelLarge)
            ColorValueTextField(
                state = state,
                format = selectedFormat,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        innerTextField()
                    }
                }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FormatButton("RGBA", selectedFormat == ColorFormat.Rgba) { selectedFormat = ColorFormat.Rgba }
            FormatButton("HSV", selectedFormat == ColorFormat.Hsv) { selectedFormat = ColorFormat.Hsv }
            FormatButton("HEX", selectedFormat == ColorFormat.Hex) { selectedFormat = ColorFormat.Hex }
        }

        Button(onClick = {
            state.setColor(Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat()))
        }) {
            Text("Random Color")
        }
    }
}

@Composable
private fun PickerSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun FormatButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(text)
    }
}

private fun ColorFormat.label(): String = when (this) {
    ColorFormat.Rgb -> "RGB"
    ColorFormat.Rgba -> "RGBA"
    ColorFormat.Hsv -> "HSV"
    ColorFormat.Hsl -> "HSL"
    ColorFormat.Hwb -> "HWB"
    ColorFormat.Cmyk -> "CMYK"
    ColorFormat.Hex -> "HEX"
    ColorFormat.Hexa -> "HEXA"
    else -> "Value"
}
