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

/** RGBA color, each channel in [0,255]. */
data class RgbaColor(val r: Int, val g: Int, val b: Int, val a: Int = 255)

/** HSV(HSB) color: hue [0,360), saturation/value in [0,100]. */
data class HsvColor(val h: Int, val s: Int, val v: Int)

/** HSL color: hue [0,360), saturation/lightness in [0,100]. */
data class HslColor(val h: Int, val s: Int, val l: Int)

/** HWB color: hue [0,360), white/black in [0,100]. */
data class HwbColor(val h: Int, val w: Int, val b: Int)

/** CMYK color: cyan/magenta/yellow/black in [0,100]. */
data class CmykColor(val c: Int, val m: Int, val y: Int, val k: Int)
