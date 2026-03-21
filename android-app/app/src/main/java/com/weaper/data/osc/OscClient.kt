package com.weaper.data.osc

import android.util.Log
import com.weaper.data.preferences.AppPreferences
import com.weaper.domain.model.OscCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OSC (Open Sound Control) client that sends UDP datagrams to REAPER.
 *
 * Architecture note: OSC uses UDP for low-latency, fire-and-forget messaging,
 * which is ideal for live performance control. No connection setup overhead.
 */
@Singleton
class OscClient @Inject constructor(
    private val preferences: AppPreferences
) {
    companion object {
        private const val TAG = "OscClient"
    }

    /**
     * Sends an OSC command to the configured REAPER host.
     */
    suspend fun send(command: OscCommand, vararg args: Any): Unit = withContext(Dispatchers.IO) {
        val host = preferences.oscHost
        val port = preferences.oscPort

        if (host.isBlank()) {
            Log.w(TAG, "OSC host not configured, skipping command: ${command.address}")
            return@withContext
        }

        try {
            val message = OscMessage.build(command.address, *args)
            val address = InetAddress.getByName(host)
            val packet = DatagramPacket(message, message.size, address, port)

            DatagramSocket().use { socket ->
                socket.send(packet)
                Log.d(TAG, "OSC sent: ${command.address} to $host:$port")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send OSC command: ${command.address}", e)
            throw e
        }
    }
}
