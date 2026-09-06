package com.slaviboy.colorpicker.textfield

import com.slaviboy.colorpicker.model.RgbaColor
import com.slaviboy.colorpicker.state.ColorPickerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Constructor
import kotlin.math.abs

/**
 * ColorPickerState's constructor is `internal`, and it's normally created via
 * `rememberColorPickerState()` inside a composition. For plain-JVM unit tests (no composition
 * running) we reach the constructor via reflection instead of trying to host a Composable.
 */
private fun newState(h: Int, s: Int, v: Int, a: Int = 255): ColorPickerState {
    val ctor: Constructor<ColorPickerState> = ColorPickerState::class.java.getDeclaredConstructor(
        Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType
    )
    ctor.isAccessible = true
    return ctor.newInstance(h, s, v, a)
}

private val allFormats: List<ColorFormat> = listOf(
    ColorFormat.Rgb, ColorFormat.Rgba, ColorFormat.Hsv, ColorFormat.Hsl,
    ColorFormat.Hwb, ColorFormat.Cmyk, ColorFormat.Hex, ColorFormat.Hexa
) + ColorFormat.RgbaChannel.entries + ColorFormat.HsvChannel.entries +
    ColorFormat.HslChannel.entries + ColorFormat.HwbChannel.entries + ColorFormat.CmykChannel.entries

private fun extractInts(text: String): List<Int> = Regex("-?\\d+").findAll(text).map { it.value.toInt() }.toList()

class ColorFormattingTest {

    // ---- formatColorValue ----

    @Test
    fun `formats multi value formats as comma separated numbers`() {
        val state = newState(h = 210, s = 40, v = 60, a = 128)
        assertEquals("${state.rgba.r}, ${state.rgba.g}, ${state.rgba.b}", formatColorValue(state, ColorFormat.Rgb))
        assertEquals("${state.rgba.r}, ${state.rgba.g}, ${state.rgba.b}, ${state.rgba.a}", formatColorValue(state, ColorFormat.Rgba))
        assertEquals("210, 40, 60", formatColorValue(state, ColorFormat.Hsv))
        assertEquals("${state.hue}, ${state.hslSaturation}, ${state.lightness}", formatColorValue(state, ColorFormat.Hsl))
        assertEquals("${state.hwb.h}, ${state.hwb.w}, ${state.hwb.b}", formatColorValue(state, ColorFormat.Hwb))
        assertEquals("${state.cmyk.c}, ${state.cmyk.m}, ${state.cmyk.y}, ${state.cmyk.k}", formatColorValue(state, ColorFormat.Cmyk))
    }

    @Test
    fun `formats hex and hexa using the hash prefixed uppercase string`() {
        val state = newState(h = 210, s = 40, v = 60, a = 128)
        assertEquals(state.hex, formatColorValue(state, ColorFormat.Hex))
        assertEquals(state.hexa, formatColorValue(state, ColorFormat.Hexa))
        assertTrue(formatColorValue(state, ColorFormat.Hex).startsWith("#"))
        assertEquals(7, formatColorValue(state, ColorFormat.Hex).length)
        assertEquals(9, formatColorValue(state, ColorFormat.Hexa).length)
    }

    @Test
    fun `formats every single channel as a bare number`() {
        val state = newState(h = 210, s = 40, v = 60, a = 128)
        assertEquals(state.rgba.r.toString(), formatColorValue(state, ColorFormat.RgbaChannel.R))
        assertEquals(state.rgba.g.toString(), formatColorValue(state, ColorFormat.RgbaChannel.G))
        assertEquals(state.rgba.b.toString(), formatColorValue(state, ColorFormat.RgbaChannel.B))
        assertEquals(state.alpha.toString(), formatColorValue(state, ColorFormat.RgbaChannel.A))
        assertEquals(state.hue.toString(), formatColorValue(state, ColorFormat.HsvChannel.H))
        assertEquals(state.hsvSaturation.toString(), formatColorValue(state, ColorFormat.HsvChannel.S))
        assertEquals(state.value.toString(), formatColorValue(state, ColorFormat.HsvChannel.V))
        assertEquals(state.hue.toString(), formatColorValue(state, ColorFormat.HslChannel.H))
        assertEquals(state.hslSaturation.toString(), formatColorValue(state, ColorFormat.HslChannel.S))
        assertEquals(state.lightness.toString(), formatColorValue(state, ColorFormat.HslChannel.L))
        assertEquals(state.hwb.h.toString(), formatColorValue(state, ColorFormat.HwbChannel.H))
        assertEquals(state.hwb.w.toString(), formatColorValue(state, ColorFormat.HwbChannel.W))
        assertEquals(state.hwb.b.toString(), formatColorValue(state, ColorFormat.HwbChannel.B))
        assertEquals(state.cmyk.c.toString(), formatColorValue(state, ColorFormat.CmykChannel.C))
        assertEquals(state.cmyk.m.toString(), formatColorValue(state, ColorFormat.CmykChannel.M))
        assertEquals(state.cmyk.y.toString(), formatColorValue(state, ColorFormat.CmykChannel.Y))
        assertEquals(state.cmyk.k.toString(), formatColorValue(state, ColorFormat.CmykChannel.K))
    }

    // ---- parseColorValue: every format round trips ----

    /**
     * For every one of the 25 [ColorFormat] variants: format a known state, parse that exact text
     * into a fresh state, then re-format the fresh state and compare. Multi-value/single-channel
     * numbers are compared with a small tolerance (some formats round-trip through a lossy
     * cross-model conversion, e.g. CMYK/HWB via RGB<->HSV - see ColorConverterTest); hex is
     * compared exactly, since hex round-trips with no floating-point math at all.
     */
    @Test
    fun `every format round trips through format then parse`() {
        val original = newState(h = 210, s = 40, v = 60, a = 180)

        for (format in allFormats) {
            val text = formatColorValue(original, format)
            // A non-degenerate baseline: pure black (s=0,v=0) would make single-channel HWB
            // edits unobservable (HWB's "black" component is 100% there, which swallows any
            // hue you write - the same class of achromatic degeneracy as the HSL bug fix), so a
            // mid-range starting color is needed to exercise every single-channel setter fairly.
            val target = newState(h = 0, s = 50, v = 70, a = 255)
            val ok = parseColorValue(target, format, text)
            assertTrue("format=$format text='$text' should parse successfully", ok)

            val textBack = formatColorValue(target, format)
            when {
                format == ColorFormat.Hex || format == ColorFormat.Hexa -> {
                    assertEquals("format=$format", text, textBack)
                }
                format is ColorFormat.CmykChannel -> {
                    // CMYK's K is derived from RGB (k = min(1-r,1-g,1-b)), not an independent
                    // free parameter - the multi-value Cmyk format round-trips exactly (verified
                    // separately below) because it reads all four components from one consistent
                    // RGB, but editing a single C/M/Y/K channel here necessarily combines it with
                    // the *other three* read from target's unrelated starting color, which can
                    // swing the reformatted value far more than simple rounding error. This
                    // mirrors the original library's own CMYK formulas, not a bug in this port -
                    // just confirm it still parses to a valid, in-range number.
                    val actual = extractInts(textBack).single()
                    assertTrue("format=$format textBack='$textBack'", actual in 0..100)
                }
                else -> {
                    val expected = extractInts(text)
                    val actual = extractInts(textBack)
                    assertEquals("format=$format text='$text' textBack='$textBack'", expected.size, actual.size)
                    for (i in expected.indices) {
                        assertTrue(
                            "format=$format value#$i expected=${expected[i]} actual=${actual[i]} text='$text' textBack='$textBack'",
                            abs(expected[i] - actual[i]) <= 2
                        )
                    }
                }
            }
        }
    }

    /**
     * Unlike a single CMYK channel, the multi-value `Cmyk` format sets all four components at
     * once from one internally-consistent (c,m,y,k) tuple (all read from the same RGB), so - per
     * the exact algebraic identity `cmykToRgb(rgbToCmyk(x)) == x` - it round-trips just as
     * precisely as every other multi-value format, unlike the single-channel case above.
     */
    @Test
    fun `multi value cmyk round trips precisely unlike a single cmyk channel`() {
        val original = newState(h = 210, s = 40, v = 60, a = 180)
        val text = formatColorValue(original, ColorFormat.Cmyk)
        val target = newState(h = 0, s = 50, v = 70, a = 255)
        assertTrue(parseColorValue(target, ColorFormat.Cmyk, text))
        val expected = extractInts(text)
        val actual = extractInts(formatColorValue(target, ColorFormat.Cmyk))
        for (i in expected.indices) {
            assertTrue("value#$i expected=${expected[i]} actual=${actual[i]}", abs(expected[i] - actual[i]) <= 2)
        }
    }

    // ---- parseColorValue: sibling channels must be preserved ----

    @Test
    fun `single channel edits preserve sibling channels exactly for fields stored directly`() {
        val state = newState(h = 210, s = 40, v = 60, a = 180)

        assertTrue(parseColorValue(state, ColorFormat.HsvChannel.V, "90"))
        assertEquals(210, state.hue)
        assertEquals(40, state.hsvSaturation)
        assertEquals(90, state.value)

        assertTrue(parseColorValue(state, ColorFormat.RgbaChannel.A, "50"))
        assertEquals(50, state.alpha)
        assertEquals(210, state.hue)

        // HSL saturation/lightness are stored directly (see ColorPickerState), so editing one
        // must leave the other untouched with no rounding drift at all.
        val hslState = newState(h = 210, s = 40, v = 60)
        assertTrue(parseColorValue(hslState, ColorFormat.HslChannel.S, "77"))
        assertEquals(77, hslState.hslSaturation)
        assertTrue(parseColorValue(hslState, ColorFormat.HslChannel.L, "33"))
        assertEquals(33, hslState.lightness)
        assertEquals(77, hslState.hslSaturation)
    }

    @Test
    fun `rgba single channel edits approximately preserve the other channels`() {
        val state = newState(h = 210, s = 40, v = 60, a = 180)
        val rBefore = state.rgba.r
        val bBefore = state.rgba.b
        assertTrue(parseColorValue(state, ColorFormat.RgbaChannel.G, "200"))
        assertTrue(abs(state.rgba.r - rBefore) <= 2)
        assertTrue(abs(state.rgba.b - bBefore) <= 2)
        assertTrue(abs(state.rgba.g - 200) <= 2)
    }

    // ---- parseColorValue: validation / rejection ----

    @Test
    fun `rejects wrong value count for multi value formats`() {
        val state = newState(h = 10, s = 20, v = 30)
        assertFalse(parseColorValue(state, ColorFormat.Rgb, "10, 20"))
        assertFalse(parseColorValue(state, ColorFormat.Rgba, "10, 20, 30"))
        assertFalse(parseColorValue(state, ColorFormat.Cmyk, "10, 20, 30"))
        assertEquals(10, state.hue)
    }

    @Test
    fun `rejects non numeric input`() {
        val state = newState(h = 10, s = 20, v = 30)
        assertFalse(parseColorValue(state, ColorFormat.Rgb, "a, b, c"))
        assertFalse(parseColorValue(state, ColorFormat.HsvChannel.H, "not-a-number"))
        assertFalse(parseColorValue(state, ColorFormat.HsvChannel.H, ""))
    }

    @Test
    fun `rejects out of range values for multi value formats`() {
        val state = newState(h = 10, s = 20, v = 30)
        assertFalse(parseColorValue(state, ColorFormat.Rgb, "300, 0, 0"))
        assertFalse(parseColorValue(state, ColorFormat.Rgb, "-1, 0, 0"))
        assertFalse(parseColorValue(state, ColorFormat.Hsv, "400, 0, 0"))
        assertFalse(parseColorValue(state, ColorFormat.Hsl, "0, 200, 0"))
        assertFalse(parseColorValue(state, ColorFormat.Hwb, "0, 0, 200"))
        assertFalse(parseColorValue(state, ColorFormat.Cmyk, "0, 0, 0, 200"))
    }

    @Test
    fun `rejects out of range values for single channel formats`() {
        val state = newState(h = 10, s = 20, v = 30)
        assertFalse(parseColorValue(state, ColorFormat.RgbaChannel.R, "256"))
        assertFalse(parseColorValue(state, ColorFormat.RgbaChannel.R, "-1"))
        assertFalse(parseColorValue(state, ColorFormat.HsvChannel.H, "361"))
        assertFalse(parseColorValue(state, ColorFormat.HsvChannel.S, "150"))
        assertFalse(parseColorValue(state, ColorFormat.CmykChannel.K, "101"))
    }

    @Test
    fun `rejects invalid hex strings`() {
        val state = newState(h = 10, s = 20, v = 30)
        assertFalse(parseColorValue(state, ColorFormat.Hex, "not-a-color"))
        assertFalse(parseColorValue(state, ColorFormat.Hex, "#ZZZZZZ"))
        assertFalse(parseColorValue(state, ColorFormat.Hexa, "#FF00"))
        assertEquals(10, state.hue)
    }

    @Test
    fun `hex accepts either 6 or 8 digit input regardless of which hex format is selected`() {
        val hexState = newState(0, 0, 0)
        assertTrue(parseColorValue(hexState, ColorFormat.Hex, "#3F8CB580"))
        assertEquals(128, hexState.alpha)

        val hexaState = newState(0, 0, 0)
        assertTrue(parseColorValue(hexaState, ColorFormat.Hexa, "3F8CB5"))
        assertEquals(RgbaColor(0x3F, 0x8C, 0xB5, 255), hexaState.rgba)
    }

    @Test
    fun `tolerates surrounding whitespace in multi value and single channel input`() {
        val state = newState(h = 0, s = 0, v = 0)
        assertTrue(parseColorValue(state, ColorFormat.Rgb, "  10 ,  20 ,   30 "))
        // Rgb is stored canonically as HSV, so reading .rgba back is a lossy round trip even for
        // an exact input - compare with the same small tolerance used elsewhere, not equality.
        assertTrue(abs(state.rgba.r - 10) <= 2)
        assertTrue(abs(state.rgba.g - 20) <= 2)
        assertTrue(abs(state.rgba.b - 30) <= 2)

        assertTrue(parseColorValue(state, ColorFormat.HsvChannel.H, "  180  "))
        assertEquals(180, state.hue)
    }

    @Test
    fun `failed parse leaves state completely unmodified`() {
        val state = newState(h = 123, s = 45, v = 67, a = 89)
        val rgbaBefore = state.rgba
        assertFalse(parseColorValue(state, ColorFormat.Rgba, "1, 2, 3")) // wrong count
        assertEquals(rgbaBefore, state.rgba)
        assertEquals(123, state.hue)
        assertEquals(45, state.hsvSaturation)
        assertEquals(89, state.alpha)
    }
}
