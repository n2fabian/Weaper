package com.weaper.domain.model

data class SyncFile(
    val name: String,
    val hash: String,
    val sizeBytes: Long = 0,
    val isLocal: Boolean = false
)

data class SyncStatus(
    val totalFiles: Int,
    val syncedFiles: Int,
    val missingFiles: List<SyncFile>,
    val inProgress: Boolean = false,
    val error: String? = null
)
