package com.slaviboy.colorpicker.util

import androidx.compose.ui.geometry.Offset
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Map a raw pixel offset along an axis of length [size] into a semantic value bound to
 * [lower]..[upper] (the bounds may be given in either order, e.g. (100,0) for a slider whose
 * "0 offset" end represents the highest value). Mirrors the original library's Range.setCurrent.
 */
fun valueForOffset(offset: Float, lower: Float, upper: Float, size: Float): Float {
    if (size == 0f) return lower
    val fact = offset / size
    return lower - (lower - upper) * fact
}

/** Inverse of [valueForOffset]: the pixel offset along an axis of length [size] for a given semantic value. */
fun offsetForValue(value: Float, lower: Float, upper: Float, size: Float): Float {
    if (lower == upper) return 0f
    return size * (lower - value) / (lower - upper)
}

/**
 * Angle in degrees [0,360) of the vector (dx,dy), measured from the positive x-axis and
 * increasing CLOCKWISE (since screen +y points downward). 0 is at 3 o'clock.
 */
fun angleBetweenTwoPoints(dx: Float, dy: Float): Float {
    var degrees = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    if (degrees < 0f) degrees += 360f
    return degrees
}

/**
 * The point at [distancePx] from [center] along the direction for [angleDeg], using the same
 * clockwise convention as [angleBetweenTwoPoints]. Used to place a selector back on screen from
 * its semantic angle/distance (the inverse drawing-side operation).
 */
fun offsetForAngleDistance(center: Offset, angleDeg: Float, distancePx: Float): Offset {
    val rad = Math.toRadians((360.0 - angleDeg))
    return Offset(
        x = center.x + distancePx * cos(rad).toFloat(),
        y = center.y - distancePx * sin(rad).toFloat()
    )
}
