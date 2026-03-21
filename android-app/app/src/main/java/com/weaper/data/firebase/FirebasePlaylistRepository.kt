package com.weaper.data.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.weaper.domain.model.Playlist
import com.weaper.domain.repository.PlaylistRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePlaylistRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : PlaylistRepository {

    companion object {
        private const val TAG = "FirebasePlaylistRepo"
        private const val COLLECTION = "playlists"
    }

    override fun getPlaylists(): Flow<List<Playlist>> = callbackFlow {
        val listener = firestore.collection(COLLECTION)
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Playlist listener error", error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<Playlist>()?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun savePlaylist(playlist: Playlist) {
        try {
            if (playlist.id.isBlank()) {
                firestore.collection(COLLECTION).add(playlist).await()
            } else {
                firestore.collection(COLLECTION).document(playlist.id).set(playlist).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save playlist", e)
            throw e
        }
    }

    override suspend fun deletePlaylist(id: String) {
        firestore.collection(COLLECTION).document(id).delete().await()
    }
}
