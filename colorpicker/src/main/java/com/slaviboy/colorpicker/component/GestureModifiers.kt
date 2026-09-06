package com.slaviboy.colorpicker.component

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize

/**
 * Moves a selector to [onMove] on the raw pointer-down position, then continues following the
 * pointer while it stays pressed. Deliberately hand-rolled instead of using
 * `detectDragGestures`, whose `onDragStart` only fires after touch-slop movement - the original
 * library moves the selector immediately on `ACTION_DOWN`, and a plain tap (no movement at all)
 * must still jump the selector there. [onMove] also receives the current layout [IntSize] in
 * pixels so callers can do their own coordinate math without a separate size-tracking modifier.
 */
fun Modifier.dragToMove(onMove: (position: Offset, size: IntSize) -> Unit): Modifier =
    pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            onMove(down.position, size)
            down.consume()
            do {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.pressed }
                if (change != null) {
                    onMove(change.position, size)
                    change.consume()
                }
            } while (event.changes.any { it.pressed })
        }
    }
