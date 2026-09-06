package com.slaviboy.colorpicker.model

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorInteropTest {

    @Test
    fun `rgba to compose color and back round trips exactly`() {
        val rgba = RgbaColor(10, 200, 55, 128)
        assertEquals(rgba, rgba.toComposeColor().toRgbaColor())
    }

    @Test
    fun `toComposeColor maps each channel to its 0 to 1 fraction`() {
        val composeColor = RgbaColor(255, 0, 128, 64).toComposeColor()
        assertEquals(1f, composeColor.red, 0.001f)
        assertEquals(0f, composeColor.green, 0.001f)
        assertEquals(128 / 255f, composeColor.blue, 0.001f)
        assertEquals(64 / 255f, composeColor.alpha, 0.001f)
    }

    @Test
    fun `toRgbaColor rounds fractional channels to the nearest int`() {
        // 0.5 * 255 = 127.5, rounds up to 128
        val rgba = Color(1f, 0f, 0.5f, 1f).toRgbaColor()
        assertEquals(255, rgba.r)
        assertEquals(0, rgba.g)
        assertEquals(128, rgba.b)
        assertEquals(255, rgba.a)
    }

    @Test
    fun `toRgbaColor clamps channels outside 0 to 1`() {
        val rgba = Color(1.5f, -0.5f, 0f, 2f).toRgbaColor()
        assertEquals(255, rgba.r)
        assertEquals(0, rgba.g)
        assertEquals(0, rgba.b)
        assertEquals(255, rgba.a)
    }

    @Test
    fun `known compose colors map to the expected rgba`() {
        assertEquals(RgbaColor(255, 0, 0, 255), Color.Red.toRgbaColor())
        assertEquals(RgbaColor(0, 255, 0, 255), Color.Green.toRgbaColor())
        assertEquals(RgbaColor(0, 0, 255, 255), Color.Blue.toRgbaColor())
        assertEquals(RgbaColor(255, 255, 255, 255), Color.White.toRgbaColor())
        assertEquals(RgbaColor(0, 0, 0, 255), Color.Black.toRgbaColor())
        assertEquals(RgbaColor(0, 0, 0, 0), Color.Transparent.toRgbaColor())
    }
}
