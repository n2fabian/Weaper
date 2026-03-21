package com.weaper.domain.model

sealed class OscCommand(val address: String) {
    // Transport controls
    object Play : OscCommand("/action/40044")        // Play
    object Stop : OscCommand("/action/40047")        // Stop
    object Record : OscCommand("/action/1013")       // Record

    // Marker / Region navigation
    data class GoToMarker(val markerId: Int) : OscCommand("/marker/$markerId")
    data class GoToRegion(val regionId: Int) : OscCommand("/region/$regionId")

    // Track / Sample triggering
    data class TrackPlay(val trackId: Int) : OscCommand("/track/$trackId/play")
    data class CustomPath(val path: String, val args: List<Any> = emptyList()) : OscCommand(path)
}
