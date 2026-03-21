package com.weaper.data.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.weaper.domain.model.SetlistItem
import com.weaper.domain.repository.SetlistRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Firestore implementation of SetlistRepository.
 * Setlist metadata is stored in Firestore for multi-device sync.
 * Audio files are NOT stored here — only references.
 */
@Singleton
class FirebaseSetlistRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : SetlistRepository {

    companion object {
        private const val TAG = "FirebaseSetlistRepo"
        private const val COLLECTION = "setlists"
    }

    override fun getSetlist(): Flow<List<SetlistItem>> = callbackFlow {
        val listener = firestore.collection(COLLECTION)
            .orderBy("orderIndex")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Setlist listener error", error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<SetlistItem>()?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveSetlistItem(item: SetlistItem) {
        try {
            if (item.id.isBlank()) {
                firestore.collection(COLLECTION).add(item).await()
            } else {
                firestore.collection(COLLECTION).document(item.id).set(item).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save setlist item", e)
            throw e
        }
    }

    override suspend fun deleteSetlistItem(id: String) {
        firestore.collection(COLLECTION).document(id).delete().await()
    }

    override suspend fun reorderSetlist(items: List<SetlistItem>) {
        val batch = firestore.batch()
        items.forEachIndexed { index, item ->
            val ref = firestore.collection(COLLECTION).document(item.id)
            batch.update(ref, "orderIndex", index)
        }
        batch.commit().await()
    }

    override suspend fun syncFromRemote() {
        // Real-time listener handles sync automatically
    }
}
