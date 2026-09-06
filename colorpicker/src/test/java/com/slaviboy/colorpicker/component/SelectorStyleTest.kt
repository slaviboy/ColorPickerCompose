package com.slaviboy.colorpicker.component

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class SelectorStyleTest {

    @Test
    fun `content padding is selector radius plus half strokes when that exceeds half border`() {
        val style = SelectorStyle(radius = 12.dp, strokeWidth = 2.dp, extraStrokeWidth = 2.dp, borderWidth = 1.dp)
        // 12 + 2/2 + 2/2 = 14dp, vs border/2 = 0.5dp -> selector padding wins
        assertEquals(14.dp, style.contentPadding())
    }

    @Test
    fun `content padding falls back to half border width when selector padding is smaller`() {
        val style = SelectorStyle(radius = 0.dp, strokeWidth = 0.dp, extraStrokeWidth = 0.dp, borderWidth = 20.dp)
        assertEquals(10.dp, style.contentPadding())
    }

    @Test
    fun `content padding matches the default style`() {
        val style = SelectorStyle()
        assertEquals(14.dp, style.contentPadding())
    }

    @Test
    fun `content padding with zero everything is zero`() {
        val style = SelectorStyle(radius = 0.dp, strokeWidth = 0.dp, extraStrokeWidth = 0.dp, borderWidth = 0.dp)
        assertEquals(0.dp, style.contentPadding())
    }
}
