/*
* Copyright (C) 2020 Stanislav Georgiev
* https://github.com/slaviboy
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.slaviboy.colorpicker.model

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Pure color-model conversion math, ported from the original ColorPickerKotlin
 * library's ColorConverter. All formulas operate on plain Int/Double values so
 * they have no Android/Compose dependency and are trivially unit-testable.
 */
object ColorConverter {

    fun hsvToRgb(h: Int, s: Int, v: Int, a: Int = 255): RgbaColor {
        val hh = h / 360.0
        val ss = s / 100.0
        val vv = v / 100.0
        var r: Double
        var g: Double
        var b: Double
        if (ss == 0.0) {
            b = vv
            g = b
            r = g
        } else {
            var i = (hh * 6).toInt()
            val f = hh * 6 - i
            val p = vv * (1 - ss)
            val q = vv * (1 - f * ss)
            val t = vv * (1 - (1 - f) * ss)
            i %= 6
            when (i) {
                0 -> { r = vv; g = t; b = p }
                1 -> { r = q; g = vv; b = p }
                2 -> { r = p; g = vv; b = t }
                3 -> { r = p; g = q; b = vv }
                4 -> { r = t; g = p; b = vv }
                else -> { r = vv; g = p; b = q }
            }
        }
        return RgbaColor((r * 255).roundToInt(), (g * 255).roundToInt(), (b * 255).roundToInt(), a)
    }

    fun rgbToHsv(r: Int, g: Int, b: Int): HsvColor {
        val rr = r / 255.0
        val gg = g / 255.0
        val bb = b / 255.0
        val minV = min(min(rr, gg), bb)
        val maxV = max(max(rr, gg), bb)
        val delta = maxV - minV
        var h = 0.0
        val s = if (maxV == 0.0) 0.0 else delta / maxV
        if (maxV != minV) {
            when (maxV) {
                rr -> h = (gg - bb) / delta + if (gg < bb) 6.0 else 0.0
                gg -> h = (bb - rr) / delta + 2.0
                else -> h = (rr - gg) / delta + 4.0
            }
            h /= 6.0
        }
        return HsvColor((h * 360).roundToInt(), (s * 100).roundToInt(), (maxV * 100).roundToInt())
    }

    /** Helper used by hslToRgb, ported verbatim from the original HUEtoRGB. */
    private fun hueToRgb(p: Double, q: Double, t: Double): Double {
        var tt = t
        if (tt < 0.0) tt += 1.0
        if (tt > 1.0) tt -= 1.0
        if (tt < 1.0 / 6.0) return p + (q - p) * 6.0 * tt
        if (tt < 1.0 / 2.0) return q
        return if (tt < 2.0 / 3.0) p + (q - p) * (2.0 / 3.0 - tt) * 6.0 else p
    }

    fun hslToRgb(h: Int, s: Int, l: Int, a: Int = 255): RgbaColor {
        val hh = h / 360.0
        val ss = s / 100.0
        val ll = l / 100.0
        val r: Double
        val g: Double
        val b: Double
        if (ss == 0.0) {
            b = ll
            g = b
            r = g
        } else {
            val q = if (ll < 0.5) ll * (1 + ss) else ll + ss - ll * ss
            val p = 2 * ll - q
            r = hueToRgb(p, q, hh + 1.0 / 3.0)
            g = hueToRgb(p, q, hh)
            b = hueToRgb(p, q, hh - 1.0 / 3.0)
        }
        return RgbaColor((r * 255).roundToInt(), (g * 255).roundToInt(), (b * 255).roundToInt(), a)
    }

    fun rgbToHsl(r: Int, g: Int, b: Int): HslColor {
        val rr = r / 255.0
        val gg = g / 255.0
        val bb = b / 255.0
        val minV = min(min(rr, gg), bb)
        val maxV = max(max(rr, gg), bb)
        val delta = maxV - minV
        var h = 0.0
        var s = 0.0
        val l = (maxV + minV) / 2.0
        if (maxV != minV) {
            s = if (l > 0.5) delta / (2.0 - maxV - minV) else delta / (maxV + minV)
            when (maxV) {
                rr -> h = (gg - bb) / delta + if (gg < bb) 6.0 else 0.0
                gg -> h = (bb - rr) / delta + 2.0
                else -> h = (rr - gg) / delta + 4.0
            }
            h /= 6.0
        }
        return HslColor((h * 360).roundToInt(), (s * 100).roundToInt(), (l * 100).roundToInt())
    }

    fun hwbToRgb(h: Int, w: Int, b: Int, a: Int = 255): RgbaColor {
        var ww = w / 100.0
        var bb = b / 100.0
        val rgb = hslToRgb(h, 100, 50)
        var r = rgb.r / 255.0
        var g = rgb.g / 255.0
        var bl = rgb.b / 255.0
        val tot = ww + bb
        if (tot > 1) {
            ww /= tot
            bb /= tot
        }
        r = r * (1 - ww - bb) + ww
        g = g * (1 - ww - bb) + ww
        bl = bl * (1 - ww - bb) + ww
        return RgbaColor((r * 255).roundToInt(), (g * 255).roundToInt(), (bl * 255).roundToInt(), a)
    }

    fun rgbToHwb(r: Int, g: Int, b: Int): HwbColor {
        val rr = r / 255.0
        val gg = g / 255.0
        val bb = b / 255.0
        val maxV = max(max(rr, gg), bb)
        val minV = min(min(rr, gg), bb)
        val delta = maxV - minV
        var h = 0.0
        val black = 1 - maxV
        if (maxV != minV) {
            when (maxV) {
                rr -> h = (gg - bb) / delta + if (gg < bb) 6.0 else 0.0
                gg -> h = (bb - rr) / delta + 2.0
                else -> h = (rr - gg) / delta + 4.0
            }
            h /= 6.0
        }
        return HwbColor((h * 360).roundToInt(), (minV * 100).roundToInt(), (black * 100).roundToInt())
    }

    fun cmykToRgb(c: Int, m: Int, y: Int, k: Int, a: Int = 255): RgbaColor {
        val cc = c / 100.0
        val mm = m / 100.0
        val yy = y / 100.0
        val kk = k / 100.0
        val r = 1 - min(1.0, cc * (1 - kk) + kk)
        val g = 1 - min(1.0, mm * (1 - kk) + kk)
        val b = 1 - min(1.0, yy * (1 - kk) + kk)
        return RgbaColor((r * 255).roundToInt(), (g * 255).roundToInt(), (b * 255).roundToInt(), a)
    }

    fun rgbToCmyk(r: Int, g: Int, b: Int): CmykColor {
        val rr = r / 255.0
        val gg = g / 255.0
        val bb = b / 255.0
        var c = 0.0
        var m = 0.0
        var y = 0.0
        val k = min(min(1 - rr, 1 - gg), 1 - bb)
        if (k != 1.0) {
            c = (1 - rr - k) / (1 - k)
            m = (1 - gg - k) / (1 - k)
            y = (1 - bb - k) / (1 - k)
        }
        return CmykColor((c * 100).roundToInt(), (m * 100).roundToInt(), (y * 100).roundToInt(), (k * 100).roundToInt())
    }

    /** Format r/g/b(/a) into a "#RRGGBB" or "#RRGGBBAA" hex string. */
    fun rgbToHex(r: Int, g: Int, b: Int, a: Int = 255, includeAlpha: Boolean = false, upperCase: Boolean = true): String {
        val hex = if (includeAlpha) {
            String.format("%02x%02x%02x%02x", r, g, b, a)
        } else {
            String.format("%02x%02x%02x", r, g, b)
        }
        return "#" + if (upperCase) hex.uppercase() else hex
    }

    /** Parse a "#RRGGBB" / "RRGGBB" / "#RRGGBBAA" / "RRGGBBAA" hex string, or null if invalid. */
    fun hexToRgb(hex: String): RgbaColor? {
        val cleaned = hex.removePrefix("#")
        if (cleaned.length != 6 && cleaned.length != 8) return null
        if (!cleaned.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) return null
        val value = cleaned.toLongOrNull(16) ?: return null
        return if (cleaned.length == 6) {
            RgbaColor(
                (value shr 16 and 0xFF).toInt(),
                (value shr 8 and 0xFF).toInt(),
                (value and 0xFF).toInt(),
                255
            )
        } else {
            RgbaColor(
                (value shr 24 and 0xFF).toInt(),
                (value shr 16 and 0xFF).toInt(),
                (value shr 8 and 0xFF).toInt(),
                (value and 0xFF).toInt()
            )
        }
    }

    /** Direct HSV -> HSL cross conversion, avoids an RGB round trip. */
    fun hsvToHsl(h: Int, s: Int, v: Int): HslColor {
        val ss = s / 100.0
        val vv = v / 100.0
        val l = (2.0 - ss) * vv / 2.0
        val hslS = if (l != 0.0) {
            when {
                l == 1.0 -> 0.0
                l < 0.5 -> ss * vv / (l * 2.0)
                else -> ss * vv / (2.0 - l * 2.0)
            }
        } else {
            0.0
        }
        return HslColor(h, (hslS * 100.0).roundToInt(), (l * 100.0).roundToInt())
    }

    /** Direct HSL -> HSV cross conversion, avoids an RGB round trip. */
    fun hslToHsv(h: Int, s: Int, l: Int): HsvColor {
        val ss = s / 100.0
        val ll = l / 100.0
        val t = ss * if (ll < 0.5) ll else 1 - ll
        val v = ll + t
        val hsvS = if (ll > 0.0) 2.0 * t / v else ss
        return HsvColor(h, (hsvS * 100).roundToInt(), (v * 100).roundToInt())
    }
}
