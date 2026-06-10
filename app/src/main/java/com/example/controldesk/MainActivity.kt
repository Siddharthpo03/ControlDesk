package com.example.controldesk

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controldesk.ui.theme.ControlDeskTheme
import java.net.URI

class MainActivity : ComponentActivity() {
    private var wsClient: ControlDeskWSClient? = null
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("controldesk_prefs", Context.MODE_PRIVATE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            ControlDeskTheme {
                AppNavigator(
                    onConnect = { ip -> connectToServer(ip) },
                    onDisconnect = {
                        wsClient?.close()
                        wsClient = null
                    },
                    getWsClient = { wsClient },
                    prefs = prefs
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableImmersiveMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enableImmersiveMode()
    }

    private fun enableImmersiveMode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                window.insetsController?.let {
                    it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        )
            }
        } catch (e: Exception) {
            // Silently ignore if immersive mode fails on some devices
        }
    }

    private fun connectToServer(ip: String) {
        val uri = URI("ws://$ip:5000")
        wsClient = ControlDeskWSClient(
            serverUri = uri,
            onConnected = {},
            onDisconnected = {},
            onError = {}
        )
        wsClient?.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        wsClient?.close()
    }
}

@Composable
fun AppNavigator(
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit,
    getWsClient: () -> ControlDeskWSClient?,
    prefs: SharedPreferences
) {
    var isConnected by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf<ControlMode?>(null) }
    var setupDone by remember {
        mutableStateOf(prefs.getBoolean("setup_done", false))
    }

    when {
        !setupDone -> {
            SetupGuideScreen(
                onDone = {
                    prefs.edit().putBoolean("setup_done", true).apply()
                    setupDone = true
                }
            )
        }
        !isConnected -> {
            ConnectScreen(
                onConnect = { ip ->
                    onConnect(ip)
                    isConnected = true
                },
                onDisconnect = {
                    onDisconnect()
                    isConnected = false
                    selectedMode = null
                }
            )
        }
        selectedMode == null -> {
            ModeSelectionScreen(
                onModeSelected = { mode ->
                    selectedMode = mode
                }
            )
        }
        else -> {
            TouchpadScreen(
                wsClient = getWsClient(),
                mode = selectedMode!!,
                onDisconnect = {
                    onDisconnect()
                    isConnected = false
                    selectedMode = null
                },
                onModeChange = { newMode ->
                    selectedMode = newMode
                }
            )
        }
    }
}

@Composable
fun ConnectScreen(
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit
) {
    var ipAddress by remember { mutableStateOf("192.168.1.10") }
    var isConnected by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Not connected") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ControlDesk",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "PC Remote Controller",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(48.dp))
            OutlinedTextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("PC IP Address") },
                placeholder = { Text("e.g. 192.168.1.8") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isConnected
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = statusText,
                fontSize = 13.sp,
                color = if (isConnected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (!isConnected) {
                        statusText = "Connecting..."
                        onConnect(ipAddress)
                        isConnected = true
                        statusText = "Connected to $ipAddress"
                    } else {
                        onDisconnect()
                        isConnected = false
                        statusText = "Not connected"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isConnected) "Disconnect" else "Connect",
                    fontSize = 16.sp
                )
            }
        }
    }
}