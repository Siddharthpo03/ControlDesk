package com.example.controldesk

import androidx.compose.ui.input.pointer.PointerInputScope
import kotlin.math.abs
import kotlin.math.sqrt

data class GestureState(
    val fingerCount: Int = 0,
    val dx: Float = 0f,
    val dy: Float = 0f,
    val zoom: Float = 1f,
    val isTap: Boolean = false,
    val isSwipe: Boolean = false,
    val swipeDirection: SwipeDirection = SwipeDirection.NONE
)

enum class SwipeDirection { UP, DOWN, LEFT, RIGHT, NONE }

suspend fun PointerInputScope.detectMultiFingerGestures(
    onGestureState: (GestureState) -> Unit
) {
    awaitPointerEventScope {
        while (true) {

            // Wait until at least one finger is down
            var event = awaitPointerEvent()
            if (event.changes.none { it.pressed }) continue

            val startTime = System.currentTimeMillis()
            var maxFingers = 0
            var totalDx = 0f
            var totalDy = 0f
            var lastEmitTime = 0L
            var accDx = 0f
            var accDy = 0f
            var gesture2Type = "undecided"
            var pinchStartDist = -1f
            var scrollAccY = 0f
            var scrollAccX = 0f
            var pinchMaxDelta = 0f
            var maxScrollDelta = 0f

            // Track positions per pointer
            val lastPos = mutableMapOf<Long, Pair<Float, Float>>()

            // Initialize positions from first event
            event.changes.filter { it.pressed }.forEach {
                lastPos[it.id.value] = Pair(it.position.x, it.position.y)
            }
            maxFingers = event.changes.count { it.pressed }

            // Keep tracking until ALL fingers lift
            while (true) {
                event = awaitPointerEvent()
                val pressed = event.changes.filter { it.pressed }

                // Update max fingers — never goes down
                val currentCount = pressed.size
                if (currentCount > maxFingers) maxFingers = currentCount

                // All fingers lifted — classify and break
                if (currentCount == 0) break

                val now = System.currentTimeMillis()

                when {
                    currentCount == 1 -> {
                        val f = pressed[0]
                        val prev = lastPos[f.id.value]
                        if (prev != null) {
                            val dx = f.position.x - prev.first
                            val dy = f.position.y - prev.second
                            totalDx += dx
                            totalDy += dy

                            // Only emit mouse move for pure 1 finger gestures
                            if (maxFingers == 1) {
                                accDx = dx
                                accDy = dy
                                if (now - lastEmitTime >= 8 &&
                                    (abs(accDx) > 0.2f || abs(accDy) > 0.2f)) {
                                    lastEmitTime = now
                                    onGestureState(
                                        GestureState(fingerCount = 1, dx = accDx, dy = accDy)
                                    )
                                }
                            }
                        }
                        lastPos[f.id.value] = Pair(f.position.x, f.position.y)
                    }

                    currentCount == 2 && maxFingers == 2 -> {
                        val f1 = pressed[0]
                        val f2 = pressed[1]
                        val prev1 = lastPos[f1.id.value]
                        val prev2 = lastPos[f2.id.value]

                        if (prev1 != null && prev2 != null) {
                            val dx1 = f1.position.x - prev1.first
                            val dy1 = f1.position.y - prev1.second
                            val dx2 = f2.position.x - prev2.first
                            val dy2 = f2.position.y - prev2.second
                            val avgDx = (dx1 + dx2) / 2f
                            val avgDy = (dy1 + dy2) / 2f
                            totalDx += avgDx
                            totalDy += avgDy

                            // Pinch detection
                            val currentDist = sqrt(
                                (f1.position.x - f2.position.x) *
                                        (f1.position.x - f2.position.x) +
                                        (f1.position.y - f2.position.y) *
                                        (f1.position.y - f2.position.y)
                            )
                            if (pinchStartDist < 0) pinchStartDist = currentDist
                            val distDelta = abs(currentDist - pinchStartDist)
                            if (distDelta > pinchMaxDelta) pinchMaxDelta = distDelta
                            val scrollDelta = abs(avgDy) + abs(avgDx)
                            if (scrollDelta > maxScrollDelta) maxScrollDelta = scrollDelta

                            // Decide gesture type
                            if (gesture2Type == "undecided") {
                                if (pinchMaxDelta > 60f && pinchMaxDelta > maxScrollDelta * 3f) {
                                    gesture2Type = "pinch"
                                } else if (maxScrollDelta > 8f) {
                                    gesture2Type = "scroll"
                                }
                            }

                            if (gesture2Type == "scroll" && now - lastEmitTime >= 12) {
                                lastEmitTime = now
                                scrollAccY += avgDy
                                scrollAccX += avgDx
                                if (abs(scrollAccY) > 20f) {
                                    onGestureState(
                                        GestureState(
                                            fingerCount = 2,
                                            dy = if (scrollAccY < 0) 1f else -1f
                                        )
                                    )
                                    scrollAccY = 0f
                                }
                                if (abs(scrollAccX) > 20f) {
                                    onGestureState(
                                        GestureState(
                                            fingerCount = 2,
                                            dx = if (scrollAccX < 0) -1f else 1f,
                                            dy = 0f
                                        )
                                    )
                                    scrollAccX = 0f
                                }
                            }

                            if (gesture2Type == "pinch" && now - lastEmitTime >= 150) {
                                lastEmitTime = now
                                val zoom = currentDist / pinchStartDist
                                onGestureState(GestureState(fingerCount = 2, zoom = zoom))
                                pinchStartDist = currentDist
                            }
                        }

                        pressed.forEach {
                            lastPos[it.id.value] = Pair(it.position.x, it.position.y)
                        }
                    }

                    currentCount >= 3 -> {
                        // Track centroid for 3+ fingers
                        val validPressed = pressed.filter { lastPos.containsKey(it.id.value) }
                        if (validPressed.isNotEmpty()) {
                            val avgDx = validPressed.map {
                                it.position.x - lastPos[it.id.value]!!.first
                            }.average().toFloat()
                            val avgDy = validPressed.map {
                                it.position.y - lastPos[it.id.value]!!.second
                            }.average().toFloat()
                            totalDx += avgDx
                            totalDy += avgDy
                        }
                        pressed.forEach {
                            lastPos[it.id.value] = Pair(it.position.x, it.position.y)
                        }
                    }
                }
            }

            // Flush remaining 1 finger movement
            if (maxFingers == 1 && (abs(accDx) > 0.5f || abs(accDy) > 0.5f)) {
                onGestureState(GestureState(fingerCount = 1, dx = accDx, dy = accDy))
            }

            val duration = System.currentTimeMillis() - startTime
            val totalMovement = sqrt(totalDx * totalDx + totalDy * totalDy)

            android.util.Log.d("GESTURE",
                "CLASSIFIED: maxFingers=$maxFingers movement=$totalMovement " +
                        "dx=$totalDx dy=$totalDy duration=$duration")

            // SWIPE — check before tap for 3+ fingers
            if (maxFingers >= 3 && totalMovement > 40f) {
                val direction = when {
                    abs(totalDy) > abs(totalDx) && totalDy < -40f -> SwipeDirection.UP
                    abs(totalDy) > abs(totalDx) && totalDy > 40f  -> SwipeDirection.DOWN
                    abs(totalDx) > abs(totalDy) && totalDx < -40f -> SwipeDirection.LEFT
                    abs(totalDx) > abs(totalDy) && totalDx > 40f  -> SwipeDirection.RIGHT
                    else -> SwipeDirection.NONE
                }
                if (direction != SwipeDirection.NONE) {
                    onGestureState(
                        GestureState(
                            fingerCount = maxFingers,
                            isSwipe = true,
                            swipeDirection = direction
                        )
                    )
                }
            }

            // TAP
            if (duration < 350 && totalMovement < 30f) {
                onGestureState(GestureState(fingerCount = maxFingers, isTap = true))
            }
        }
    }
}