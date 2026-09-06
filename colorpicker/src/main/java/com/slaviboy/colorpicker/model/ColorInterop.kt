package com.slaviboy.colorpicker.model

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

/** Convert this RGBA color model to a Compose [Color]. */
fun RgbaColor.toComposeColor(): Color = Color(
    red = r / 255f,
    green = g / 255f,
    blue = b / 255f,
    alpha = a / 255f
)

/** Convert this Compose [Color] to an [RgbaColor], rounding each channel to [0,255]. */
fun Color.toRgbaColor(): RgbaColor = RgbaColor(
    r = (red * 255f).roundToInt().coerceIn(0, 255),
    g = (green * 255f).roundToInt().coerceIn(0, 255),
    b = (blue * 255f).roundToInt().coerceIn(0, 255),
    a = (alpha * 255f).roundToInt().coerceIn(0, 255)
)
