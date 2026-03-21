package com.weaper.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.weaper.domain.model.SoundboardItem
import com.weaper.domain.repository.SoundboardRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Firestore implementation of SoundboardRepository.
 * Stores soundboard button metadata (labels, OSC paths, file references).
 * Checks local filesystem to determine if audio files are available.
 */
@Singleton
class FirebaseSoundboardRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) : SoundboardRepository {

    companion object {
        private const val TAG = "FirebaseSoundboardRepo"
        private const val COLLECTION = "soundboard"
    }

    override fun getSoundboard(): Flow<List<SoundboardItem>> = callbackFlow {
        val listener = firestore.collection(COLLECTION)
            .orderBy("orderIndex")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Soundboard listener error", error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<SoundboardItem>()?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveSoundboardItem(item: SoundboardItem) {
        try {
            if (item.id.isBlank()) {
                firestore.collection(COLLECTION).add(item).await()
            } else {
                firestore.collection(COLLECTION).document(item.id).set(item).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save soundboard item", e)
            throw e
        }
    }

    override suspend fun deleteSoundboardItem(id: String) {
        firestore.collection(COLLECTION).document(id).delete().await()
    }

    override suspend fun syncFromRemote() {
        // Real-time listener handles sync automatically
    }

    override suspend fun updateAvailability(items: List<SoundboardItem>): List<SoundboardItem> {
        // Use app-specific external directory (same as LocalSyncRepository) for consistency
        val syncDir = File(context.getExternalFilesDir(null), "Weaper")
        return items.map { item ->
            item.copy(isAvailable = File(syncDir, item.fileName).exists())
        }
    }
}
