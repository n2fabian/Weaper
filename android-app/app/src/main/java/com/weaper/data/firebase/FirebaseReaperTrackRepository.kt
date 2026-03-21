package com.weaper.data.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.weaper.domain.model.ReaperTrack
import com.weaper.domain.repository.ReaperTrackRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseReaperTrackRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReaperTrackRepository {

    companion object {
        private const val TAG = "FirebaseReaperTrackRepo"
        private const val COLLECTION = "reaper_tracks"
    }

    override fun getTracks(): Flow<List<ReaperTrack>> = callbackFlow {
        val listener = firestore.collection(COLLECTION)
            .orderBy("title")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Track listener error", error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<ReaperTrack>()?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveTrack(track: ReaperTrack) {
        try {
            if (track.id.isBlank()) {
                firestore.collection(COLLECTION).add(track).await()
            } else {
                firestore.collection(COLLECTION).document(track.id).set(track).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save track", e)
            throw e
        }
    }

    override suspend fun deleteTrack(id: String) {
        firestore.collection(COLLECTION).document(id).delete().await()
    }
}
