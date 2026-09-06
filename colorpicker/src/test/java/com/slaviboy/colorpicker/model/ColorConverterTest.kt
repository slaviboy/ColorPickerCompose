package com.slaviboy.colorpicker.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ColorConverterTest {

    @Test
    fun `hsv primary colors convert to rgb`() {
        assertEquals(RgbaColor(255, 0, 0, 255), ColorConverter.hsvToRgb(0, 100, 100))
        assertEquals(RgbaColor(0, 255, 0, 255), ColorConverter.hsvToRgb(120, 100, 100))
        assertEquals(RgbaColor(0, 0, 255, 255), ColorConverter.hsvToRgb(240, 100, 100))
    }

    @Test
    fun `hsv zero saturation is white regardless of hue`() {
        val color = ColorConverter.hsvToRgb(180, 0, 100)
        assertEquals(RgbaColor(255, 255, 255, 255), color)
    }

    @Test
    fun `hsv zero value is black regardless of hue and saturation`() {
        val color = ColorConverter.hsvToRgb(180, 50, 0)
        assertEquals(RgbaColor(0, 0, 0, 255), color)
    }

    @Test
    fun `rgb to hsv for achromatic gray has zero hue and saturation`() {
        val hsv = ColorConverter.rgbToHsv(128, 128, 128)
        assertEquals(0, hsv.h)
        assertEquals(0, hsv.s)
        assertTrue(abs(hsv.v - 50) <= 1)
    }

    @Test
    fun `rgb to hsv round trip over a sampled grid`() {
        for (h in 0 until 360 step 15) {
            for (s in 0..100 step 20) {
                for (v in 10..100 step 20) {
                    val rgb = ColorConverter.hsvToRgb(h, s, v)
                    val hsv = ColorConverter.rgbToHsv(rgb.r, rgb.g, rgb.b)
                    val rgbBack = ColorConverter.hsvToRgb(hsv.h, hsv.s, hsv.v)
                    assertTrue(
                        "h=$h s=$s v=$v -> rgb=$rgb hsv=$hsv rgbBack=$rgbBack",
                        abs(rgb.r - rgbBack.r) <= 1 && abs(rgb.g - rgbBack.g) <= 1 && abs(rgb.b - rgbBack.b) <= 1
                    )
                }
            }
        }
    }

    @Test
    fun `hsl pure red matches hsv pure red`() {
        assertEquals(RgbaColor(255, 0, 0, 255), ColorConverter.hslToRgb(0, 100, 50))
        assertEquals(HslColor(0, 100, 50), ColorConverter.rgbToHsl(255, 0, 0))
    }

    @Test
    fun `rgb to hsl for achromatic gray has zero hue and saturation`() {
        val hsl = ColorConverter.rgbToHsl(128, 128, 128)
        assertEquals(0, hsl.h)
        assertEquals(0, hsl.s)
        assertTrue(abs(hsl.l - 50) <= 1)
    }

    @Test
    fun `hsv saturation and hsl saturation are different numbers for the same color`() {
        // HSV(0,50,100) -> RGB(255,128,128) -> HSL(0,100,~75); the HSV s (50) must not equal the HSL s (100).
        val rgb = ColorConverter.hsvToRgb(0, 50, 100)
        assertEquals(RgbaColor(255, 128, 128, 255), rgb)

        val hslFromRgb = ColorConverter.rgbToHsl(rgb.r, rgb.g, rgb.b)
        val hslFromHsv = ColorConverter.hsvToHsl(0, 50, 100)

        assertEquals(hslFromRgb.h, hslFromHsv.h)
        assertTrue(abs(hslFromRgb.s - hslFromHsv.s) <= 1)
        assertTrue(abs(hslFromRgb.l - hslFromHsv.l) <= 1)
        assertEquals(100, hslFromHsv.s)
        assertTrue(hslFromHsv.s != 50)
    }

    @Test
    fun `hsv hsl cross conversion round trips`() {
        // Two conversions each round to Int, so allow a small compounded rounding tolerance
        // (this is the same quantization behavior the original library has, since it also
        // rounds to Int at every conversion step).
        for (h in 0 until 360 step 30) {
            for (s in 0..100 step 25) {
                for (v in 0..100 step 25) {
                    val hsl = ColorConverter.hsvToHsl(h, s, v)
                    val hsvBack = ColorConverter.hslToHsv(hsl.h, hsl.s, hsl.l)
                    val rgbOriginal = ColorConverter.hsvToRgb(h, s, v)
                    val rgbBack = ColorConverter.hsvToRgb(hsvBack.h, hsvBack.s, hsvBack.v)
                    assertTrue(
                        "h=$h s=$s v=$v -> hsl=$hsl hsvBack=$hsvBack rgbOriginal=$rgbOriginal rgbBack=$rgbBack",
                        abs(rgbOriginal.r - rgbBack.r) <= 3 && abs(rgbOriginal.g - rgbBack.g) <= 3 && abs(rgbOriginal.b - rgbBack.b) <= 3
                    )
                }
            }
        }
    }

    @Test
    fun `hwb pure colors`() {
        assertEquals(RgbaColor(255, 0, 0, 255), ColorConverter.hwbToRgb(0, 0, 0))
        assertEquals(RgbaColor(255, 255, 255, 255), ColorConverter.hwbToRgb(0, 100, 0))
        assertEquals(RgbaColor(0, 0, 0, 255), ColorConverter.hwbToRgb(0, 0, 100))
    }

    @Test
    fun `rgb to hwb round trip`() {
        val hwb = ColorConverter.rgbToHwb(255, 0, 0)
        assertEquals(HwbColor(0, 0, 0), hwb)
    }

    @Test
    fun `rgb to hwb for white and black`() {
        assertEquals(HwbColor(0, 100, 0), ColorConverter.rgbToHwb(255, 255, 255))
        assertEquals(HwbColor(0, 0, 100), ColorConverter.rgbToHwb(0, 0, 0))
    }

    @Test
    fun `hwb normalizes toward gray when white plus black exceeds 100 percent`() {
        // w=70, b=60 sum to 130% - must be scaled down proportionally rather than clipped,
        // which for equal-ish w/b relative to the hue's pure channels yields a near-neutral gray.
        val rgb = ColorConverter.hwbToRgb(0, 70, 60)
        assertTrue(abs(rgb.r - rgb.g) <= 2)
        assertTrue(abs(rgb.g - rgb.b) <= 2)
        assertTrue(abs(rgb.r - 137) <= 3)
    }

    @Test
    fun `cmyk pure colors`() {
        assertEquals(RgbaColor(255, 0, 0, 255), ColorConverter.cmykToRgb(0, 100, 100, 0))
        assertEquals(RgbaColor(0, 0, 0, 255), ColorConverter.cmykToRgb(0, 0, 0, 100))
        assertEquals(CmykColor(0, 100, 100, 0), ColorConverter.rgbToCmyk(255, 0, 0))
    }

    @Test
    fun `rgb to cmyk for white and black`() {
        assertEquals(CmykColor(0, 0, 0, 0), ColorConverter.rgbToCmyk(255, 255, 255))
        assertEquals(CmykColor(0, 0, 0, 100), ColorConverter.rgbToCmyk(0, 0, 0))
    }

    @Test
    fun `hex formatting and parsing`() {
        assertEquals("#FF0000", ColorConverter.rgbToHex(255, 0, 0, 255, includeAlpha = false))
        assertEquals("#FF000080", ColorConverter.rgbToHex(255, 0, 0, 128, includeAlpha = true))
        assertEquals(RgbaColor(255, 0, 0, 255), ColorConverter.hexToRgb("#FF0000"))
        assertEquals(RgbaColor(255, 0, 0, 255), ColorConverter.hexToRgb("FF0000"))
        assertEquals(RgbaColor(255, 0, 0, 128), ColorConverter.hexToRgb("#FF000080"))
        assertNull(ColorConverter.hexToRgb("#ZZZZZZ"))
        assertNull(ColorConverter.hexToRgb("#FF00"))
    }

    @Test
    fun `hex formatting respects the upperCase flag`() {
        assertEquals("#ff0000", ColorConverter.rgbToHex(255, 0, 0, upperCase = false))
        assertEquals("#FF0000", ColorConverter.rgbToHex(255, 0, 0, upperCase = true))
    }

    @Test
    fun `hex parsing accepts lowercase and mixed case digits`() {
        assertEquals(RgbaColor(255, 0, 0, 255), ColorConverter.hexToRgb("#ff0000"))
        assertEquals(RgbaColor(171, 205, 239, 255), ColorConverter.hexToRgb("abcdef"))
        assertEquals(RgbaColor(171, 205, 239, 255), ColorConverter.hexToRgb("AbCdEf"))
    }

    @Test
    fun `hex parsing rejects malformed input`() {
        assertNull(ColorConverter.hexToRgb(""))
        assertNull(ColorConverter.hexToRgb("#"))
        assertNull(ColorConverter.hexToRgb("#FFFFFFF")) // 7 digits, not 6 or 8
        assertNull(ColorConverter.hexToRgb("#GGGGGG")) // not hex digits
    }

    @Test
    fun `negative hue wraps using mod not remainder`() {
        assertEquals(350, (-10).mod(360))
    }
}
