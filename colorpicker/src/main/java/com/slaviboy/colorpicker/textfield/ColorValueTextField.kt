package com.slaviboy.colorpicker.textfield

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slaviboy.colorpicker.state.ColorPickerState
import com.slaviboy.colorpicker.state.rememberColorPickerState

/**
 * A single-line, format-aware text field bound directly to a [ColorPickerState] - the Compose
 * replacement for the original library's tag/View-attachment TextView system. The user can type
 * freely; on every keystroke a valid value is pushed live into [state] (so other pickers sharing
 * the same state move immediately), while an invalid/partial value is shown as-is without
 * touching [state]. On focus loss, if the current text doesn't parse, it snaps back to the
 * current formatted value of [state].
 */
@Composable
fun ColorValueTextField(
    state: ColorPickerState,
    format: ColorFormat,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    cursorBrush: Brush = SolidColor(Color.Black),
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit = { it() },
) {
    var rawText by remember(format) { mutableStateOf(formatColorValue(state, format)) }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Re-derive the displayed text from state whenever it changes from another source
    // (another picker, a random-color button, ...) while this field isn't the one being edited.
    val liveFormatted = formatColorValue(state, format)
    if (!isFocused && rawText != liveFormatted) {
        rawText = liveFormatted
    }

    BasicTextField(
        value = rawText,
        onValueChange = { newText ->
            rawText = newText
            parseColorValue(state, format, newText)
        },
        modifier = modifier.onFocusChanged { focusState ->
            val wasFocused = isFocused
            isFocused = focusState.isFocused
            if (wasFocused && !focusState.isFocused && !parseColorValue(state, format, rawText)) {
                rawText = formatColorValue(state, format)
            }
        },
        textStyle = textStyle,
        cursorBrush = cursorBrush,
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        singleLine = true,
        decorationBox = decorationBox
    )
}

@Preview(showBackground = true)
@Composable
private fun ColorValueTextFieldPreview() {
    ColorValueTextField(
        state = rememberColorPickerState(Color(0xFF3F8CB5)),
        format = ColorFormat.Hex,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                innerTextField()
            }
        }
    )
}
