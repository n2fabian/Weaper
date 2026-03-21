package com.weaper.data.osc

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

/**
 * Lightweight OSC (Open Sound Control) message builder.
 * Encodes OSC 1.0 messages as byte arrays for UDP transmission.
 *
 * OSC packets consist of:
 * - Address pattern (null-terminated, padded to 4 bytes)
 * - Type tag string starting with ',' (null-terminated, padded to 4 bytes)
 * - Arguments (each padded to 4 bytes)
 */
object OscMessage {

    /**
     * Builds a raw OSC message byte array.
     * @param address The OSC address pattern (e.g., "/transport/play")
     * @param args Optional arguments (Int, Float, String supported)
     */
    fun build(address: String, vararg args: Any): ByteArray {
        val buffer = ByteBuffer.allocate(512)

        // Write address
        writeString(buffer, address)

        // Build type tag string
        val typeTags = buildString {
            append(',')
            for (arg in args) {
                when (arg) {
                    is Int -> append('i')
                    is Float -> append('f')
                    is String -> append('s')
                    else -> append('i')
                }
            }
        }
        writeString(buffer, typeTags)

        // Write arguments
        for (arg in args) {
            when (arg) {
                is Int -> buffer.putInt(arg)
                is Float -> buffer.putFloat(arg)
                is String -> writeString(buffer, arg)
            }
        }

        val size = buffer.position()
        return buffer.array().copyOf(size)
    }

    private fun writeString(buffer: ByteBuffer, str: String) {
        val bytes = str.toByteArray(StandardCharsets.UTF_8)
        buffer.put(bytes)
        buffer.put(0) // null terminator
        // Round up to next 4-byte boundary: add 3 then clear the low 2 bits (3.inv() = 0xFFFFFFFC)
        val padded = (bytes.size + 1 + 3) and 3.inv()
        val padding = padded - (bytes.size + 1)
        repeat(padding) { buffer.put(0) }
    }
}
