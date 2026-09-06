package com.slaviboy.colorpicker.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.slaviboy.colorpicker.model.CmykColor
import com.slaviboy.colorpicker.model.ColorConverter
import com.slaviboy.colorpicker.model.HslColor
import com.slaviboy.colorpicker.model.HwbColor
import com.slaviboy.colorpicker.model.RgbaColor
import com.slaviboy.colorpicker.model.toComposeColor
import com.slaviboy.colorpicker.model.toRgbaColor

/**
 * Single reactive source of truth for a color picker (or a group of pickers sharing one
 * instance). Canonical state is HSV ([hue],[hsvSaturation],[value]) + [alpha]; everything else
 * (HSL, HWB, CMYK, HEX, the final RGBA [color]) is derived on every read, never cached, so there
 * is no possibility of a stale copy.
 *
 * Every color-window composable reads only the fields it needs and calls one of the setters
 * below when the user drags its selector; because Compose recomposition re-runs any composable
 * whose read state changed, no manual "notify attached windows" step is needed.
 */
@Stable
class ColorPickerState internal constructor(h: Int, s: Int, v: Int, a: Int) {

    // `.mod()` (not `%`) so a negative input wraps forward, e.g. -10 -> 350 instead of -10.
    private val hueState = mutableIntStateOf(h.mod(360))
    private val hsvSaturationState = mutableIntStateOf(s.coerceIn(0, 100))
    private val valueState = mutableIntStateOf(v.coerceIn(0, 100))
    private val alphaState = mutableIntStateOf(a.coerceIn(0, 255))

    val hue: Int get() = hueState.intValue
    val hsvSaturation: Int get() = hsvSaturationState.intValue
    val value: Int get() = valueState.intValue
    val alpha: Int get() = alphaState.intValue

    // ---- derived, read-only ----

    val hsl: HslColor get() = ColorConverter.hsvToHsl(hue, hsvSaturation, value)
    val hslSaturation: Int get() = hsl.s
    val lightness: Int get() = hsl.l

    val rgba: RgbaColor get() = ColorConverter.hsvToRgb(hue, hsvSaturation, value, alpha)
    val color: Color get() = rgba.toComposeColor()

    /** The pure hue color (S=100,V=100), used to render hue-dependent backgrounds. */
    val baseHueColor: Color get() = ColorConverter.hsvToRgb(hue, 100, 100).toComposeColor()

    val hwb: HwbColor get() = ColorConverter.rgbToHwb(rgba.r, rgba.g, rgba.b)
    val cmyk: CmykColor get() = ColorConverter.rgbToCmyk(rgba.r, rgba.g, rgba.b)
    val hex: String get() = ColorConverter.rgbToHex(rgba.r, rgba.g, rgba.b, alpha, includeAlpha = false)
    val hexa: String get() = ColorConverter.rgbToHex(rgba.r, rgba.g, rgba.b, alpha, includeAlpha = true)

    // ---- writers, one per gesture-driving window (plus a few convenience overwrites) ----

    /** Used by HueSlider and HueSaturationWheel. */
    fun setHue(h: Int) {
        hueState.intValue = h.mod(360)
    }

    /** Used by ValueSlider. */
    fun setValue(v: Int) {
        valueState.intValue = v.coerceIn(0, 100)
    }

    /** Used by AlphaSlider. */
    fun setAlpha(a: Int) {
        alphaState.intValue = a.coerceIn(0, 255)
    }

    /** Used by HueSaturationWheel (CircularHS). */
    fun setHueSaturation(h: Int, s: Int) {
        hueState.intValue = h.mod(360)
        hsvSaturationState.intValue = s.coerceIn(0, 100)
    }

    /** Used by SaturationValueSquare (RectangularSV). */
    fun setSaturationValue(s: Int, v: Int) {
        hsvSaturationState.intValue = s.coerceIn(0, 100)
        valueState.intValue = v.coerceIn(0, 100)
    }

    /** Used by SaturationLightnessSquare (RectangularSL); cross-converts HSL -> HSV canonical fields. */
    fun setSaturationLightness(s: Int, l: Int) {
        val hsv = ColorConverter.hslToHsv(hue, s.coerceIn(0, 100), l.coerceIn(0, 100))
        hsvSaturationState.intValue = hsv.s
        valueState.intValue = hsv.v
    }

    /** Full overwrite from an arbitrary Compose [Color] (e.g. a "random color" button). */
    fun setColor(newColor: Color) {
        val rgba = newColor.toRgbaColor()
        setRgba(rgba.r, rgba.g, rgba.b, rgba.a)
    }

    fun setRgba(r: Int, g: Int, b: Int, a: Int) {
        val hsv = ColorConverter.rgbToHsv(r, g, b)
        hueState.intValue = hsv.h
        hsvSaturationState.intValue = hsv.s
        valueState.intValue = hsv.v
        alphaState.intValue = a.coerceIn(0, 255)
    }

    /** Returns false (and leaves the state unmodified) if [hexString] isn't a valid hex color. */
    fun setHexString(hexString: String): Boolean {
        val rgba = ColorConverter.hexToRgb(hexString) ?: return false
        setRgba(rgba.r, rgba.g, rgba.b, rgba.a)
        return true
    }

    fun setCmyk(c: Int, m: Int, y: Int, k: Int) {
        val rgb = ColorConverter.cmykToRgb(c, m, y, k)
        setRgba(rgb.r, rgb.g, rgb.b, alpha)
    }

    fun setHwb(h: Int, w: Int, b: Int) {
        val rgb = ColorConverter.hwbToRgb(h, w, b, alpha)
        setRgba(rgb.r, rgb.g, rgb.b, alpha)
    }
}

/**
 * Creates and remembers a [ColorPickerState] seeded from [initial]. [initial] is only read on
 * first composition (standard `remember` semantics) - to change the color later (e.g. a "random
 * color" button), call [ColorPickerState.setColor] on the returned instance directly.
 */
@Composable
fun rememberColorPickerState(initial: Color = Color.Red): ColorPickerState = remember {
    val rgba = initial.toRgbaColor()
    val hsv = ColorConverter.rgbToHsv(rgba.r, rgba.g, rgba.b)
    ColorPickerState(hsv.h, hsv.s, hsv.v, rgba.a)
}
