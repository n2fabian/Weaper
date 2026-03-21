package com.weaper.domain.repository

import com.weaper.domain.model.SoundboardItem
import kotlinx.coroutines.flow.Flow

interface SoundboardRepository {
    fun getSoundboard(): Flow<List<SoundboardItem>>
    suspend fun saveSoundboardItem(item: SoundboardItem)
    suspend fun deleteSoundboardItem(id: String)
    suspend fun syncFromRemote()
    suspend fun updateAvailability(items: List<SoundboardItem>): List<SoundboardItem>
}
