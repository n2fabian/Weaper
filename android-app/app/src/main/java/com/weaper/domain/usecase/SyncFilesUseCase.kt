package com.weaper.domain.usecase

import com.weaper.domain.model.SyncFile
import com.weaper.domain.model.SyncStatus
import com.weaper.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Compares local and remote file lists using filename + hash (delta sync),
 * then uploads only missing or changed files.
 */
class SyncFilesUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    operator fun invoke(): Flow<SyncStatus> = flow {
        emit(SyncStatus(0, 0, emptyList(), inProgress = true))

        val remoteFiles = syncRepository.getRemoteFileList()
        val localFiles = syncRepository.getLocalFileList()

        // Delta sync: find files that differ by hash or are missing remotely
        val remoteMap = remoteFiles.associateBy { it.name }
        val missing = localFiles.filter { local ->
            val remote = remoteMap[local.name]
            remote == null || remote.hash != local.hash
        }

        if (missing.isEmpty()) {
            emit(SyncStatus(localFiles.size, localFiles.size, emptyList(), inProgress = false))
            return@flow
        }

        syncRepository.syncFiles(missing).collect { status ->
            emit(status)
        }
    }
}
