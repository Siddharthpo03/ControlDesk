package com.example.controldesk

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlin.math.sqrt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun TouchpadScreen(
    wsClient: ControlDeskWSClient?,
    mode: ControlMode,
    onDisconnect: () -> Unit,
    onModeChange: (ControlMode) -> Unit
) {

    val remainderX = remember { floatArrayOf(0f) }
    val remainderY = remember { floatArrayOf(0f) }
    var gestureHint by remember { mutableStateOf("Touch to control") }
    var isDragging by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val touchpadModifier = Modifier
        .background(
            color = if (isDragging)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
        .pointerInput(Unit) {
            detectMultiFingerGestures { state ->
                when {
                    // ── Taps ──
                    state.isTap && state.fingerCount == 1 -> {
                        wsClient?.sendCommand("LEFT_CLICK")
                        gestureHint = "Left click"
                    }
                    state.isTap && state.fingerCount == 2 -> {
                        wsClient?.sendCommand("RIGHT_CLICK")
                        gestureHint = "Right click"
                    }
                    state.isTap && state.fingerCount == 3 -> {
                        wsClient?.sendCommand("MIDDLE_CLICK")
                        gestureHint = "Middle click"
                    }
                    state.isTap && state.fingerCount >= 4 -> {
                        wsClient?.sendCommand("TAP_4")
                        gestureHint = "4 finger tap"
                    }

                    // ── 3 finger swipes ──
                    state.isSwipe && state.fingerCount == 3 -> {
                        val action = when (state.swipeDirection) {
                            SwipeDirection.UP    -> "SWIPE_3_UP"
                            SwipeDirection.DOWN  -> "SWIPE_3_DOWN"
                            SwipeDirection.LEFT  -> "SWIPE_3_LEFT"
                            SwipeDirection.RIGHT -> "SWIPE_3_RIGHT"
                            else -> null
                        }
                        val hint = when (state.swipeDirection) {
                            SwipeDirection.UP    -> "Task view"
                            SwipeDirection.DOWN  -> "Show desktop"
                            SwipeDirection.LEFT  -> "Prev desktop"
                            SwipeDirection.RIGHT -> "Next desktop"
                            else -> ""
                        }
                        action?.let {
                            wsClient?.sendCommand(it)
                            gestureHint = hint
                        }
                    }

                    // ── 4 finger swipes ──
                    state.isSwipe && state.fingerCount >= 4 -> {
                        val action = when (state.swipeDirection) {
                            SwipeDirection.UP   -> "SWIPE_4_UP"
                            SwipeDirection.DOWN -> "SWIPE_4_DOWN"
                            else -> null
                        }
                        action?.let {
                            wsClient?.sendCommand(it)
                            gestureHint = "4 finger swipe"
                        }
                    }

                    // ── 2 finger scroll ──
                    state.fingerCount == 2 && !state.isTap && state.zoom == 1f -> {

                        val scrollSpeed = kotlin.math.sqrt(
                            state.dx * state.dx +
                                    state.dy * state.dy
                        )

                        val scrollMultiplier =
                            (1f + scrollSpeed * 0.15f)
                                .coerceIn(1f, 5f)

                        val finalDx = state.dx * scrollMultiplier
                        val finalDy = state.dy * scrollMultiplier

                        if (finalDy != 0f) {
                            wsClient?.sendCommand(
                                "SCROLL",
                                mapOf(
                                    "dy" to (-finalDy).roundToInt()
                                )
                            )

                            gestureHint =
                                if (finalDy > 0)
                                    "Scroll down"
                                else
                                    "Scroll up"
                        }

                        if (finalDx != 0f) {
                            wsClient?.sendCommand(
                                "SCROLL_H",
                                mapOf(
                                    "dx" to (-finalDx).roundToInt()
                                )
                            )
                        }
                    }

                    // ── Pinch zoom ──
                    state.fingerCount == 2 && state.zoom != 1f -> {
                        if (state.zoom > 1.15f) {
                            wsClient?.sendCommand("ZOOM_IN")
                            gestureHint = "Zoom in"
                        } else if (state.zoom < 0.85f) {
                            wsClient?.sendCommand("ZOOM_OUT")
                            gestureHint = "Zoom out"
                        }
                    }

                    // ── 1 finger move ──
                    state.fingerCount == 1 && !state.isTap -> {

                        val speed = kotlin.math.sqrt(
                            state.dx * state.dx +
                                    state.dy * state.dy
                        )

                        // Smooth only very slow movements
                        val smoothDx =
                            if (speed < 3f)
                                state.dx * 0.8f
                            else
                                state.dx

                        val smoothDy =
                            if (speed < 3f)
                                state.dy * 0.8f
                            else
                                state.dy

                        val multiplier =
                            (1f + speed * 0.08f)
                                .coerceIn(1f, 4f)

                        val finalDx = smoothDx * multiplier
                        val finalDy = smoothDy * multiplier

                        remainderX[0] += finalDx
                        remainderY[0] += finalDy

                        val sendDx = remainderX[0].toInt()
                        val sendDy = remainderY[0].toInt()

                        remainderX[0] -= sendDx
                        remainderY[0] -= sendDy

                        if (sendDx != 0 || sendDy != 0) {
                            wsClient?.sendCommand(
                                "MOUSE_MOVE",
                                mapOf(
                                    "dx" to sendDx,
                                    "dy" to sendDy
                                )
                            )
                        }

                        gestureHint = "Moving"
                    }
                }
            }
        }

    if (isLandscape) {
        // ── Landscape Layout ──
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = touchpadModifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = gestureHint,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            Column(
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModeTab("Std", mode == ControlMode.STANDARD) {
                        onModeChange(ControlMode.STANDARD)
                    }
                    ModeTab("Hyb", mode == ControlMode.HYBRID) {
                        onModeChange(ControlMode.HYBRID)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (mode == ControlMode.HYBRID) {
                    Button(
                        onClick = {
                            if (!isDragging) {
                                wsClient?.sendCommand("DRAG_START")
                                isDragging = true
                                gestureHint = "Dragging..."
                            } else {
                                wsClient?.sendCommand("DRAG_END")
                                isDragging = false
                                gestureHint = "Touch to control"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDragging)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = if (isDragging) "Release" else "Drag",
                            fontSize = 13.sp,
                            color = if (isDragging)
                                MaterialTheme.colorScheme.onError
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Button(
                        onClick = { wsClient?.sendCommand("LEFT_CLICK") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Left", fontSize = 13.sp) }

                    Button(
                        onClick = { wsClient?.sendCommand("MIDDLE_CLICK") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) { Text("Middle", fontSize = 13.sp) }

                    Button(
                        onClick = { wsClient?.sendCommand("RIGHT_CLICK") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) { Text("Right", fontSize = 13.sp) }
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = onDisconnect,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Exit", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        }

    } else {
        // ── Portrait Layout ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ControlDesk",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModeTab("Standard", mode == ControlMode.STANDARD) {
                        onModeChange(ControlMode.STANDARD)
                    }
                    ModeTab("Hybrid", mode == ControlMode.HYBRID) {
                        onModeChange(ControlMode.HYBRID)
                    }
                }
                TextButton(onClick = onDisconnect) {
                    Text("Exit", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = touchpadModifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = gestureHint,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (mode == ControlMode.HYBRID) {
                Button(
                    onClick = {
                        if (!isDragging) {
                            wsClient?.sendCommand("DRAG_START")
                            isDragging = true
                            gestureHint = "Dragging... tap to release"
                        } else {
                            wsClient?.sendCommand("DRAG_END")
                            isDragging = false
                            gestureHint = "Touch to control"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDragging)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = if (isDragging) "Release Drag" else "Hold & Drag",
                        color = if (isDragging)
                            MaterialTheme.colorScheme.onError
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { wsClient?.sendCommand("LEFT_CLICK") },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Left") }

                    Button(
                        onClick = { wsClient?.sendCommand("MIDDLE_CLICK") },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) { Text("Middle") }

                    Button(
                        onClick = { wsClient?.sendCommand("RIGHT_CLICK") },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) { Text("Right") }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ModeTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}