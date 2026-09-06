package com.slaviboy.colorpicker.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
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
 * instance). Canonical state is HSV ([hue],[hsvSaturation],[value]) + [alpha], plus a mirrored
 * ([hslSaturation],[lightness]) pair for HSL - see the comment on those fields for why HSL isn't
 * purely derived from HSV like everything else.
 *
 * Every color-window composable reads only the fields it needs and calls one of the setters
 * below when the user drags its selector; because Compose recomposition re-runs any composable
 * whose read state changed, no manual "notify attached windows" step is needed.
 */
@Stable
class ColorPickerState internal constructor(h: Int, s: Int, v: Int, a: Int) {

    // Hue is stored in the CLOSED range [0,360], not wrapped into [0,359]. 0 and 360 are the same
    // red, but the Hue slider's track has both ends showing that red - if hue only ever stored 0,
    // dragging to the top of the slider (which computes hue=360, then would wrap to 0) would
    // redraw the selector back at the *bottom* (0's position), visibly jumping away from the
    // finger. Letting hue reach exactly 360 keeps "top" and "bottom" distinguishable. Every
    // conversion in ColorConverter already treats hue 360 identically to 0 (the `i %= 6` sector
    // step), so this loses no correctness elsewhere (e.g. the hue/saturation wheel).
    private val hueState = mutableIntStateOf(h.coerceIn(0, 360))
    private val hsvSaturationState = mutableIntStateOf(s.coerceIn(0, 100))
    private val valueState = mutableIntStateOf(v.coerceIn(0, 100))
    private val alphaState = mutableIntStateOf(a.coerceIn(0, 255))

    // HSL saturation/lightness are stored explicitly (mirroring HSV, similar to the original
    // library's ColorConverter) rather than derived from HSV on every read, because HSV<->HSL is
    // a *lossy* conversion at the achromatic extremes: at lightness 0 (black) or 100 (white),
    // every saturation value maps to the same color, so converting HSV back to HSL always reports
    // saturation 0 there. If hslSaturation were purely derived, dragging the Saturation/Lightness
    // square to its top or bottom edge (any saturation, but lightness 0 or 100) would compute
    // hsvSaturation=0 correctly, but then redrawing the selector *from* hsvSaturation would report
    // saturation 0 too and snap the selector to the opposite (right) edge - even though nothing
    // about the position the user dragged to actually changed. Storing them explicitly lets the
    // selector stay exactly where it was dropped; they're kept in sync with HSV in both
    // directions by [syncHslFromHsv] (best-effort, lossy) and [setSaturationLightness]
    // (authoritative, exact) respectively.
    private val hslSaturationState: MutableIntState
    private val lightnessState: MutableIntState

    init {
        val initialHsl = ColorConverter.hsvToHsl(hueState.intValue, hsvSaturationState.intValue, valueState.intValue)
        hslSaturationState = mutableIntStateOf(initialHsl.s)
        lightnessState = mutableIntStateOf(initialHsl.l)
    }

    val hue: Int get() = hueState.intValue
    val hsvSaturation: Int get() = hsvSaturationState.intValue
    val value: Int get() = valueState.intValue
    val alpha: Int get() = alphaState.intValue
    val hslSaturation: Int get() = hslSaturationState.intValue
    val lightness: Int get() = lightnessState.intValue

    // ---- derived, read-only ----

    val hsl: HslColor get() = HslColor(hue, hslSaturation, lightness)

    val rgba: RgbaColor get() = ColorConverter.hsvToRgb(hue, hsvSaturation, value, alpha)
    val color: Color get() = rgba.toComposeColor()

    /** The pure hue color (S=100,V=100), used to render hue-dependent backgrounds. */
    val baseHueColor: Color get() = ColorConverter.hsvToRgb(hue, 100, 100).toComposeColor()

    val hwb: HwbColor get() = ColorConverter.rgbToHwb(rgba.r, rgba.g, rgba.b)
    val cmyk: CmykColor get() = ColorConverter.rgbToCmyk(rgba.r, rgba.g, rgba.b)
    val hex: String get() = ColorConverter.rgbToHex(rgba.r, rgba.g, rgba.b, alpha, includeAlpha = false)
    val hexa: String get() = ColorConverter.rgbToHex(rgba.r, rgba.g, rgba.b, alpha, includeAlpha = true)

    /** Best-effort resync of the HSL mirror after an HSV-side field changed; see the field comment above. */
    private fun syncHslFromHsv() {
        val hsl = ColorConverter.hsvToHsl(hue, hsvSaturation, value)
        hslSaturationState.intValue = hsl.s
        lightnessState.intValue = hsl.l
    }

    // ---- writers, one per gesture-driving window (plus a few convenience overwrites) ----

    /** Used by HueSlider and HueSaturationWheel. */
    fun setHue(h: Int) {
        hueState.intValue = h.coerceIn(0, 360)
        syncHslFromHsv()
    }

    /** Used by ValueSlider. */
    fun setValue(v: Int) {
        valueState.intValue = v.coerceIn(0, 100)
        syncHslFromHsv()
    }

    /** Used by AlphaSlider. Alpha doesn't affect hue/saturation/lightness, so no HSL resync needed. */
    fun setAlpha(a: Int) {
        alphaState.intValue = a.coerceIn(0, 255)
    }

    /** Used by HueSaturationWheel (CircularHS). */
    fun setHueSaturation(h: Int, s: Int) {
        hueState.intValue = h.coerceIn(0, 360)
        hsvSaturationState.intValue = s.coerceIn(0, 100)
        syncHslFromHsv()
    }

    /** Used by SaturationValueSquare (RectangularSV). */
    fun setSaturationValue(s: Int, v: Int) {
        hsvSaturationState.intValue = s.coerceIn(0, 100)
        valueState.intValue = v.coerceIn(0, 100)
        syncHslFromHsv()
    }

    /**
     * Used by SaturationLightnessSquare (RectangularSL). Stores (s,l) exactly (see the field
     * comment above), then cross-converts to HSV so every other window stays in sync.
     */
    fun setSaturationLightness(s: Int, l: Int) {
        hslSaturationState.intValue = s.coerceIn(0, 100)
        lightnessState.intValue = l.coerceIn(0, 100)
        val hsv = ColorConverter.hslToHsv(hue, hslSaturation, lightness)
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
        syncHslFromHsv()
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
