package com.weaper.domain.repository

import com.weaper.domain.model.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun savePlaylist(playlist: Playlist): String
    suspend fun deletePlaylist(id: String)
}
