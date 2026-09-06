package com.slaviboy.colorpicker.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Visual styling for a color window's selector and border, replacing the original library's XML
 * attributes (selector_radius, selector_stroke_width, selector_extra_stroke_*, border_*).
 */
data class SelectorStyle(
    val radius: Dp = 12.dp,
    val strokeWidth: Dp = 2.dp,
    val strokeColor: Color = Color.White,
    val extraStrokeWidth: Dp = 2.dp,
    val extraStrokeColor: Color = Color.Black.copy(alpha = 0.35f),
    val borderColor: Color = Color.Black.copy(alpha = 0.18f),
    val borderWidth: Dp = 1.dp
)

/**
 * Padding to reserve on every side of a color window so the selector never gets clipped, ported
 * verbatim from the original Base.onInitBase padding formula.
 */
fun SelectorStyle.contentPadding(): Dp {
    val selectorPadding = radius + strokeWidth / 2 + extraStrokeWidth / 2
    val borderPadding = borderWidth / 2
    return if (selectorPadding > borderPadding) selectorPadding else borderPadding
}
