package com.slaviboy.colorpicker.util

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class RangeMathTest {

    @Test
    fun `value and offset for value are inverses`() {
        val v = valueForOffset(offset = 30f, lower = 0f, upper = 100f, size = 120f)
        val backOffset = offsetForValue(v, lower = 0f, upper = 100f, size = 120f)
        assertTrue(abs(backOffset - 30f) < 0.001f)
    }

    @Test
    fun `inverted range maps offset zero to lower bound`() {
        // Mirrors sliders like Hue/Value whose Range is (upper, lower) inverted, e.g. Range(360,0).
        val v = valueForOffset(offset = 0f, lower = 360f, upper = 0f, size = 100f)
        assertEquals(360f, v, 0.001f)
        val vEnd = valueForOffset(offset = 100f, lower = 360f, upper = 0f, size = 100f)
        assertEquals(0f, vEnd, 0.001f)
    }

    @Test
    fun `angle from a touch point and back places the selector at the same point`() {
        val center = Offset(100f, 100f)
        for (angle in 0 until 360 step 15) {
            val rad = Math.toRadians(angle.toDouble())
            val distance = 40f
            val touch = Offset(
                center.x + distance * Math.cos(rad).toFloat(),
                center.y + distance * Math.sin(rad).toFloat()
            )
            val measuredAngle = angleBetweenTwoPoints(touch.x - center.x, touch.y - center.y)
            val placed = offsetForAngleDistance(center, measuredAngle, distance)
            assertTrue(
                "angle=$angle measuredAngle=$measuredAngle touch=$touch placed=$placed",
                abs(placed.x - touch.x) < 0.01f && abs(placed.y - touch.y) < 0.01f
            )
        }
    }

    @Test
    fun `angle zero is at three o'clock and increases clockwise on screen`() {
        // dx>0, dy=0 (3 o'clock) -> 0 degrees
        assertEquals(0f, angleBetweenTwoPoints(10f, 0f), 0.001f)
        // dx=0, dy>0 (6 o'clock, straight down on screen) -> 90 degrees (clockwise from 3 o'clock)
        assertEquals(90f, angleBetweenTwoPoints(0f, 10f), 0.001f)
        // dx<0, dy=0 (9 o'clock) -> 180 degrees
        assertEquals(180f, angleBetweenTwoPoints(-10f, 0f), 0.001f)
        // dx=0, dy<0 (12 o'clock, straight up on screen) -> 270 degrees
        assertEquals(270f, angleBetweenTwoPoints(0f, -10f), 0.001f)
    }
}
