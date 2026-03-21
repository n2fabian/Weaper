package com.weaper.domain.repository

import com.weaper.domain.model.SetlistItem
import kotlinx.coroutines.flow.Flow

interface SetlistRepository {
    fun getSetlist(): Flow<List<SetlistItem>>
    suspend fun saveSetlistItem(item: SetlistItem)
    suspend fun deleteSetlistItem(id: String)
    suspend fun reorderSetlist(items: List<SetlistItem>)
    suspend fun syncFromRemote()
}
