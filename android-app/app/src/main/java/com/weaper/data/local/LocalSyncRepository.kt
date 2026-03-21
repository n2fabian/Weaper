package com.weaper.data.local

import android.content.Context
import android.util.Log
import com.weaper.data.network.SyncApiClient
import com.weaper.domain.model.SyncFile
import com.weaper.domain.model.SyncStatus
import com.weaper.domain.repository.SyncRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiClient: SyncApiClient
) : SyncRepository {

    companion object {
        private const val TAG = "LocalSyncRepository"
        private const val SYNC_DIR = "Weaper"
    }

    private val syncDirectory: File
        get() = File(context.getExternalFilesDir(null), SYNC_DIR).also { it.mkdirs() }

    override suspend fun getRemoteFileList(): List<SyncFile> {
        return try {
            val response = apiClient.getService().getFileList()
            response.files.map { dto ->
                SyncFile(name = dto.name, hash = dto.hash, sizeBytes = dto.size)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get remote file list", e)
            emptyList()
        }
    }

    override suspend fun getLocalFileList(): List<SyncFile> {
        val dir = syncDirectory
        if (!dir.exists()) return emptyList()

        return dir.listFiles()
            ?.filter { it.isFile && (it.extension == "mp3" || it.extension == "wav") }
            ?.map { file ->
                SyncFile(
                    name = file.name,
                    hash = file.computeMd5(),
                    sizeBytes = file.length(),
                    isLocal = true
                )
            } ?: emptyList()
    }

    override fun syncFiles(filesToSync: List<SyncFile>): Flow<SyncStatus> = flow {
        var synced = 0
        val total = filesToSync.size

        for (file in filesToSync) {
            emit(SyncStatus(total, synced, filesToSync.drop(synced), inProgress = true))
            val localFile = File(syncDirectory, file.name)
            if (localFile.exists()) {
                val success = uploadFile(localFile.absolutePath)
                if (success) synced++
            }
        }

        emit(SyncStatus(total, synced, filesToSync.drop(synced), inProgress = false))
    }

    override suspend fun uploadFile(localPath: String): Boolean {
        return try {
            val file = File(localPath)
            val requestBody = file.asRequestBody("audio/*".toMediaType())
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val response = apiClient.getService().uploadFile(part)
            response.success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload file: $localPath", e)
            false
        }
    }

    private fun File.computeMd5(): String {
        val md = MessageDigest.getInstance("MD5")
        inputStream().use { stream ->
            val buffer = ByteArray(8192)
            var read: Int
            while (stream.read(buffer).also { read = it } != -1) {
                md.update(buffer, 0, read)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
