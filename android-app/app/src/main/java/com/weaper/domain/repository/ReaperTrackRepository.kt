package com.weaper.domain.repository

import com.weaper.domain.model.ReaperTrack
import kotlinx.coroutines.flow.Flow

interface ReaperTrackRepository {
    fun getTracks(): Flow<List<ReaperTrack>>
    suspend fun saveTrack(track: ReaperTrack)
    suspend fun deleteTrack(id: String)
}
