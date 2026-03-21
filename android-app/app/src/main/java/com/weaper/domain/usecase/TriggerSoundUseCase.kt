package com.weaper.domain.usecase

import com.weaper.data.osc.OscClient
import com.weaper.domain.model.OscCommand
import com.weaper.domain.model.SoundboardItem
import javax.inject.Inject

/**
 * Triggers a soundboard sample in REAPER via OSC.
 * Uses custom OSC path if defined, otherwise falls back to track play.
 */
class TriggerSoundUseCase @Inject constructor(
    private val oscClient: OscClient
) {
    suspend operator fun invoke(item: SoundboardItem): Result<Unit> = runCatching {
        val command = if (item.oscPath.isNotBlank()) {
            OscCommand.CustomPath(item.oscPath)
        } else {
            OscCommand.TrackPlay(item.trackId)
        }
        oscClient.send(command)
    }
}
