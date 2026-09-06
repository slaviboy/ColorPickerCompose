package com.slaviboy.colorpicker.textfield

/** Every color format/channel [com.slaviboy.colorpicker.textfield.ColorValueTextField] can show and edit. */
sealed interface ColorFormat {
    data object Rgb : ColorFormat
    data object Rgba : ColorFormat
    data object Hsv : ColorFormat
    data object Hsl : ColorFormat
    data object Hwb : ColorFormat
    data object Cmyk : ColorFormat
    data object Hex : ColorFormat
    data object Hexa : ColorFormat

    enum class RgbaChannel : ColorFormat { R, G, B, A }
    enum class HsvChannel : ColorFormat { H, S, V }
    enum class HslChannel : ColorFormat { H, S, L }
    enum class HwbChannel : ColorFormat { H, W, B }
    enum class CmykChannel : ColorFormat { C, M, Y, K }
}
