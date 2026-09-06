package com.slaviboy.colorpicker.textfield

import com.slaviboy.colorpicker.state.ColorPickerState

/** Render [state]'s current value in [format] as plain comma-separated numbers (or "#RRGGBB" for hex). */
fun formatColorValue(state: ColorPickerState, format: ColorFormat): String = when (format) {
    ColorFormat.Rgb -> with(state.rgba) { "$r, $g, $b" }
    ColorFormat.Rgba -> with(state.rgba) { "$r, $g, $b, $a" }
    ColorFormat.Hsv -> "${state.hue}, ${state.hsvSaturation}, ${state.value}"
    ColorFormat.Hsl -> "${state.hue}, ${state.hslSaturation}, ${state.lightness}"
    ColorFormat.Hwb -> with(state.hwb) { "$h, $w, $b" }
    ColorFormat.Cmyk -> with(state.cmyk) { "$c, $m, $y, $k" }
    ColorFormat.Hex -> state.hex
    ColorFormat.Hexa -> state.hexa
    ColorFormat.RgbaChannel.R -> state.rgba.r.toString()
    ColorFormat.RgbaChannel.G -> state.rgba.g.toString()
    ColorFormat.RgbaChannel.B -> state.rgba.b.toString()
    ColorFormat.RgbaChannel.A -> state.alpha.toString()
    ColorFormat.HsvChannel.H -> state.hue.toString()
    ColorFormat.HsvChannel.S -> state.hsvSaturation.toString()
    ColorFormat.HsvChannel.V -> state.value.toString()
    ColorFormat.HslChannel.H -> state.hue.toString()
    ColorFormat.HslChannel.S -> state.hslSaturation.toString()
    ColorFormat.HslChannel.L -> state.lightness.toString()
    ColorFormat.HwbChannel.H -> state.hwb.h.toString()
    ColorFormat.HwbChannel.W -> state.hwb.w.toString()
    ColorFormat.HwbChannel.B -> state.hwb.b.toString()
    ColorFormat.CmykChannel.C -> state.cmyk.c.toString()
    ColorFormat.CmykChannel.M -> state.cmyk.m.toString()
    ColorFormat.CmykChannel.Y -> state.cmyk.y.toString()
    ColorFormat.CmykChannel.K -> state.cmyk.k.toString()
}

private fun parseInts(text: String, count: Int): List<Int>? {
    val parts = text.split(",").map { it.trim() }
    if (parts.size != count) return null
    return parts.map { it.toIntOrNull() ?: return null }
}

private fun inRange(value: Int, lower: Int, upper: Int) = value in lower..upper

/**
 * Try to parse [text] as [format] and, on success, write it into [state]. Returns false (and
 * leaves [state] unmodified) if [text] doesn't parse or any value is out of range.
 */
fun parseColorValue(state: ColorPickerState, format: ColorFormat, text: String): Boolean {
    when (format) {
        ColorFormat.Rgb -> {
            val v = parseInts(text, 3) ?: return false
            if (!v.all { inRange(it, 0, 255) }) return false
            state.setRgba(v[0], v[1], v[2], state.alpha)
        }
        ColorFormat.Rgba -> {
            val v = parseInts(text, 4) ?: return false
            if (!v.all { inRange(it, 0, 255) }) return false
            state.setRgba(v[0], v[1], v[2], v[3])
        }
        ColorFormat.Hsv -> {
            val v = parseInts(text, 3) ?: return false
            if (!inRange(v[0], 0, 360) || !inRange(v[1], 0, 100) || !inRange(v[2], 0, 100)) return false
            state.setHueSaturation(v[0], v[1])
            state.setValue(v[2])
        }
        ColorFormat.Hsl -> {
            val v = parseInts(text, 3) ?: return false
            if (!inRange(v[0], 0, 360) || !inRange(v[1], 0, 100) || !inRange(v[2], 0, 100)) return false
            state.setHue(v[0])
            state.setSaturationLightness(v[1], v[2])
        }
        ColorFormat.Hwb -> {
            val v = parseInts(text, 3) ?: return false
            if (!inRange(v[0], 0, 360) || !inRange(v[1], 0, 100) || !inRange(v[2], 0, 100)) return false
            state.setHwb(v[0], v[1], v[2])
        }
        ColorFormat.Cmyk -> {
            val v = parseInts(text, 4) ?: return false
            if (!v.all { inRange(it, 0, 100) }) return false
            state.setCmyk(v[0], v[1], v[2], v[3])
        }
        // Hex and Hexa share one parser: setHexString already accepts both 6- and 8-digit
        // strings, so typing either length is accepted regardless of which format is selected.
        ColorFormat.Hex -> {
            if (!state.setHexString(text)) return false
        }
        ColorFormat.Hexa -> {
            if (!state.setHexString(text)) return false
        }
        ColorFormat.RgbaChannel.R -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 255)) return false
            state.setRgba(v, state.rgba.g, state.rgba.b, state.alpha)
        }
        ColorFormat.RgbaChannel.G -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 255)) return false
            state.setRgba(state.rgba.r, v, state.rgba.b, state.alpha)
        }
        ColorFormat.RgbaChannel.B -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 255)) return false
            state.setRgba(state.rgba.r, state.rgba.g, v, state.alpha)
        }
        ColorFormat.RgbaChannel.A -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 255)) return false
            state.setAlpha(v)
        }
        ColorFormat.HsvChannel.H -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 360)) return false
            state.setHue(v)
        }
        ColorFormat.HsvChannel.S -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 100)) return false
            state.setSaturationValue(v, state.value)
        }
        ColorFormat.HsvChannel.V -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 100)) return false
            state.setValue(v)
        }
        ColorFormat.HslChannel.H -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 360)) return false
            state.setHue(v)
        }
        ColorFormat.HslChannel.S -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 100)) return false
            state.setSaturationLightness(v, state.lightness)
        }
        ColorFormat.HslChannel.L -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 100)) return false
            state.setSaturationLightness(state.hslSaturation, v)
        }
        ColorFormat.HwbChannel.H -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 360)) return false
            state.setHwb(v, state.hwb.w, state.hwb.b)
        }
        ColorFormat.HwbChannel.W -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 100)) return false
            state.setHwb(state.hwb.h, v, state.hwb.b)
        }
        ColorFormat.HwbChannel.B -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 100)) return false
            state.setHwb(state.hwb.h, state.hwb.w, v)
        }
        ColorFormat.CmykChannel.C -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 100)) return false
            state.setCmyk(v, state.cmyk.m, state.cmyk.y, state.cmyk.k)
        }
        ColorFormat.CmykChannel.M -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 100)) return false
            state.setCmyk(state.cmyk.c, v, state.cmyk.y, state.cmyk.k)
        }
        ColorFormat.CmykChannel.Y -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 100)) return false
            state.setCmyk(state.cmyk.c, state.cmyk.m, v, state.cmyk.k)
        }
        ColorFormat.CmykChannel.K -> {
            val v = text.trim().toIntOrNull() ?: return false
            if (!inRange(v, 0, 100)) return false
            state.setCmyk(state.cmyk.c, state.cmyk.m, state.cmyk.y, v)
        }
    }
    return true
}
