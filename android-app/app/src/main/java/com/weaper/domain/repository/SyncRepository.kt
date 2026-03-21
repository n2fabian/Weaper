package com.weaper.domain.repository

import com.weaper.domain.model.SyncFile
import com.weaper.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    suspend fun getRemoteFileList(): List<SyncFile>
    suspend fun getLocalFileList(): List<SyncFile>
    fun syncFiles(filesToSync: List<SyncFile>): Flow<SyncStatus>
    suspend fun uploadFile(localPath: String): Boolean
}
