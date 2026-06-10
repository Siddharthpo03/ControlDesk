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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SetupGuideScreen(onDone: () -> Unit) {
    val steps = listOf(
        Triple("📸", "Disable 3-finger screenshot",
            "Go to Settings → Additional Settings → Button shortcuts → Turn OFF '3-finger screenshot'"),
        Triple("🖐️", "Disable gesture shortcuts",
            "Go to Settings → Additional Settings → Full screen gestures → Disable any 3 or 4 finger gestures"),
        Triple("⚡", "Disable Quick Ball",
            "Go to Settings → Special Features → Quick Ball → Turn OFF"),
        Triple("♿", "Check Accessibility gestures",
            "Go to Settings → Accessibility → Any gesture shortcuts → Turn OFF")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header
            Text(
                text = "⚙️",
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "One-time Setup",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "To use all gestures properly, please\ndisable conflicting system gestures.",
                fontSize = 14.sp,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Brand note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFF1A1A1A),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "ℹ️  Steps below are for MIUI/Xiaomi. " +
                            "On other phones, look for similar gesture settings.",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Steps
            steps.forEachIndexed { index, (emoji, title, description) ->
                StepCard(
                    number = index + 1,
                    emoji = emoji,
                    title = title,
                    description = description
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Done button
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF534AB7)
                )
            ) {
                Text(
                    text = "I've done this, Let's Go! →",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onDone) {
                Text(
                    text = "Skip for now",
                    color = Color(0xFF555555),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StepCard(
    number: Int,
    emoji: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Number badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF534AB7), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$number",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = "$emoji  $title",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF888888),
                lineHeight = 18.sp
            )
        }
    }
}