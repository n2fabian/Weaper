package com.weaper.domain.usecase

import com.weaper.data.osc.OscClient
import com.weaper.domain.model.OscCommand
import com.weaper.domain.model.SetlistItem
import javax.inject.Inject

/**
 * Sends OSC commands to REAPER to navigate to a setlist item's marker/region
 * and optionally start playback.
 */
class PlaySetlistItemUseCase @Inject constructor(
    private val oscClient: OscClient
) {
    suspend operator fun invoke(item: SetlistItem): Result<Unit> = runCatching {
        // Navigate to the marker or region
        if (item.regionId > 0) {
            oscClient.send(OscCommand.GoToRegion(item.regionId))
        } else {
            oscClient.send(OscCommand.GoToMarker(item.markerId))
        }
        // Start playback if configured
        if (item.autoPlay) {
            oscClient.send(OscCommand.Play)
        }
    }
}
