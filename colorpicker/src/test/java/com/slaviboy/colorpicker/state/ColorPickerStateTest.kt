package com.slaviboy.colorpicker.state

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
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

class ColorPickerStateTest {

    @Test
    fun `setSaturationLightness round trips through hslSaturation and lightness`() {
        val state = newState(h = 210, s = 50, v = 80)
        state.setSaturationLightness(s = 60, l = 40)
        assertTrue(abs(state.hslSaturation - 60) <= 1)
        assertTrue(abs(state.lightness - 40) <= 1)
    }

    @Test
    fun `setHue does not perturb alpha`() {
        val state = newState(h = 0, s = 50, v = 50, a = 77)
        state.setHue(200)
        assertEquals(77, state.alpha)
    }

    @Test
    fun `setColor red yields full hsv red`() {
        val state = newState(h = 0, s = 0, v = 0)
        state.setColor(Color.Red)
        assertEquals(0, state.hue)
        assertEquals(100, state.hsvSaturation)
        assertEquals(100, state.value)
    }

    @Test
    fun `setAlpha only changes alpha`() {
        val state = newState(h = 90, s = 40, v = 60)
        state.setAlpha(128)
        assertEquals(90, state.hue)
        assertEquals(40, state.hsvSaturation)
        assertEquals(60, state.value)
        assertEquals(128, state.alpha)
    }

    @Test
    fun `setHexString rejects invalid input without mutating state`() {
        val state = newState(h = 10, s = 20, v = 30)
        val ok = state.setHexString("not-a-color")
        assertEquals(false, ok)
        assertEquals(10, state.hue)
        assertEquals(20, state.hsvSaturation)
        assertEquals(30, state.value)
    }

    @Test
    fun `setHueSaturation clamps out of range inputs`() {
        val state = newState(h = 0, s = 0, v = 50)
        state.setHueSaturation(h = -10, s = 150)
        assertEquals(0, state.hue)
        assertEquals(100, state.hsvSaturation)
    }

    // Regression test for a reported bug: dragging the Hue slider to its very top computed
    // hue=360, which used to wrap to 0 and then redraw the selector at the *bottom* (0's
    // position), visibly jumping away from the finger. Hue must be able to reach 360 itself,
    // distinct from 0, so the top of the slider round-trips back to the top.
    @Test
    fun `setHue allows 360 distinct from 0`() {
        val state = newState(h = 10, s = 50, v = 50)
        state.setHue(360)
        assertEquals(360, state.hue)
    }

    @Test
    fun `hue 360 and hue 0 produce the same color`() {
        val state360 = newState(h = 0, s = 80, v = 80).also { it.setHue(360) }
        val state0 = newState(h = 0, s = 80, v = 80).also { it.setHue(0) }
        assertEquals(state0.rgba, state360.rgba)
    }

    // Regression test for a reported bug: dragging the Saturation/Lightness square to its top or
    // bottom edge (lightness 100 or 0) always snapped the selector to the right edge, because
    // HSV<->HSL is lossy there - every saturation maps to the same achromatic color, so deriving
    // hslSaturation from the (now-degenerate) HSV fields always reported 0. hslSaturation must be
    // preserved exactly as dragged, even at a degenerate lightness.
    @Test
    fun `setSaturationLightness preserves saturation at lightness 100`() {
        val state = newState(h = 210, s = 50, v = 50)
        state.setSaturationLightness(s = 100, l = 100)
        assertEquals(100, state.hslSaturation)
        assertEquals(100, state.lightness)
        // The color is still correctly white (saturation is meaningless at l=100).
        assertEquals(0, state.hsvSaturation)
        assertEquals(100, state.value)
    }

    @Test
    fun `setSaturationLightness preserves saturation at lightness 0`() {
        val state = newState(h = 210, s = 50, v = 50)
        state.setSaturationLightness(s = 100, l = 0)
        assertEquals(100, state.hslSaturation)
        assertEquals(0, state.lightness)
    }

    @Test
    fun `hslSaturation recovers once lightness leaves the degenerate edge`() {
        val state = newState(h = 210, s = 50, v = 50)
        state.setSaturationLightness(s = 100, l = 100)
        state.setSaturationLightness(s = 100, l = 50)
        assertTrue(abs(state.hslSaturation - 100) <= 1)
    }
}
