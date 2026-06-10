package com.example.controldesk

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class ControlDeskWSClient(
    serverUri: URI,
    private val onConnected: () -> Unit,
    private val onDisconnected: () -> Unit,
    private val onError: (String) -> Unit
) : WebSocketClient(serverUri) {

    override fun onOpen(handshakedata: ServerHandshake?) {
        onConnected()
    }

    override fun onMessage(message: String?) {
        // We don't expect messages from server for now
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        onDisconnected()
    }

    override fun onError(ex: Exception?) {
        onError(ex?.message ?: "Unknown error")
    }

    fun sendCommand(action: String, params: Map<String, Any> = emptyMap()) {
        if (isOpen) {
            val json = buildString {
                append("{\"action\":\"$action\"")
                params.forEach { (key, value) ->
                    append(",\"$key\":")
                    if (value is String) append("\"$value\"")
                    else append(value)
                }
                append("}")
            }
            send(json)
        }
    }
}