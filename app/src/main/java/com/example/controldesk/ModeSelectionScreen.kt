package com.example.controldesk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ControlMode { STANDARD, HYBRID }

@Composable
fun ModeSelectionScreen(onModeSelected: (ControlMode) -> Unit) {

    val standardColor = Color(0xFF534AB7)
    val standardBg = Color(0xFF1a1a2e)
    val standardLight = Color(0xFFAFA9EC)
    val standardDark = Color(0xFF26215C)

    val hybridColor = Color(0xFF0F6E56)
    val hybridBg = Color(0xFF0f2027)
    val hybridLight = Color(0xFF5DCAA5)
    val hybridDark = Color(0xFF04342C)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111111)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "ControlDesk",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Choose your control mode",
                fontSize = 14.sp,
                color = Color(0xFF888888),
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Standard Mode Card
            ModeCard(
                title = "Standard",
                badge = "Trackpad style",
                description = "Multi-finger gestures just like a laptop trackpad. Natural and intuitive.",
                pills = listOf("1 finger move", "2 finger scroll", "3 finger swipe", "pinch zoom", "tap to click"),
                cardBg = standardBg,
                accentColor = standardColor,
                lightColor = standardLight,
                darkColor = standardDark,
                buttonText = "Use Standard",
                onClick = { onModeSelected(ControlMode.STANDARD) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hybrid Mode Card
            ModeCard(
                title = "Hybrid",
                badge = "Mouse style",
                description = "All trackpad gestures plus dedicated physical-style buttons for precision.",
                pills = listOf("click buttons", "scroll strip", "click + drag", "all gestures"),
                cardBg = hybridBg,
                accentColor = hybridColor,
                lightColor = hybridLight,
                darkColor = hybridDark,
                buttonText = "Use Hybrid",
                onClick = { onModeSelected(ControlMode.HYBRID) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "You can switch modes anytime from the touchpad screen",
                fontSize = 12.sp,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    badge: String,
    description: String,
    pills: List<String>,
    cardBg: Color,
    accentColor: Color,
    lightColor: Color,
    darkColor: Color,
    buttonText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .padding(20.dp)
    ) {
        // Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(darkColor)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(text = badge, fontSize = 11.sp, color = lightColor)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = lightColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Description
        Text(
            text = description,
            fontSize = 13.sp,
            color = lightColor.copy(alpha = 0.7f),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Pills
        FlowRow(pills = pills, pillBg = darkColor, pillText = lightColor)

        Spacer(modifier = Modifier.height(20.dp))

        // Button
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text(text = buttonText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun FlowRow(pills: List<String>, pillBg: Color, pillText: Color) {
    val rows = pills.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { pill ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(pillBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = pill, fontSize = 11.sp, color = pillText)
                    }
                }
            }
        }
    }
}