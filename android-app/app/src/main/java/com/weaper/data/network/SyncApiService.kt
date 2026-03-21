package com.weaper.data.network

import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class RemoteFileDto(
    val name: String = "",
    val hash: String = "",
    val size: Long = 0
)

data class FileListResponse(
    val files: List<RemoteFileDto> = emptyList()
)

data class UploadResponse(
    val success: Boolean = false,
    val message: String = ""
)

/**
 * Retrofit interface for the local Node.js sync server.
 * The server runs on the MacBook and handles file storage/retrieval.
 */
interface SyncApiService {

    @GET("/files")
    suspend fun getFileList(): FileListResponse

    @Multipart
    @POST("/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): UploadResponse
}
