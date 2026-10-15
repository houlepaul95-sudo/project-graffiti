package com.example.data

import android.util.Log
import com.example.model.GraffitiFontStyle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GraffitiRepository(
    private val graffitiDao: GraffitiDao,
    private val firestore: FirebaseFirestore? = null
) {
    val allSaved: Flow<List<SavedGraffiti>> = graffitiDao.getAllSaved()

    suspend fun save(item: SavedGraffiti): Long {
        val insertedId = graffitiDao.insertGraffiti(item)
        val fullItem = if (item.id == 0L) item.copy(id = insertedId) else item

        // If user is authenticated, sync immediately to Firestore cloud
        syncSingleToCloud(fullItem)
        return insertedId
    }

    suspend fun delete(id: Long) {
        graffitiDao.deleteById(id)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null && firestore != null) {
            try {
                firestore.collection("users")
                    .document(uid)
                    .collection("creations")
                    .document(id.toString())
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.w("GraffitiRepo", "Cloud delete failed: ${e.message}")
            }
        }
    }

    private suspend fun syncSingleToCloud(item: SavedGraffiti) = withContext(Dispatchers.IO) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext
        val db = firestore ?: return@withContext
        try {
            val data = hashMapOf(
                "id" to item.id.toString(),
                "userId" to uid,
                "title" to item.title,
                "text" to item.text,
                "fontStyle" to item.fontStyleName,
                "fillColorIndex" to item.fillColorIndex,
                "isGradient" to item.isGradient,
                "gradientColorIndex" to item.gradientColorIndex,
                "outlineColorIndex" to item.outlineColorIndex,
                "wallIndex" to item.wallIndex,
                "lean" to item.lean,
                "splats" to item.splats,
                "drips" to item.drips,
                "highlights" to item.highlights,
                "outlineThickness" to item.outlineThickness.toDouble(),
                "shadowDepth" to item.shadowDepth.toDouble(),
                "splatSeed" to item.splatSeed,
                "splatDensity" to item.splatDensity,
                "createdAt" to item.createdAt
            )
            db.collection("users")
                .document(uid)
                .collection("creations")
                .document(item.id.toString())
                .set(data, SetOptions.merge())
                .await()
            Log.d("GraffitiRepo", "Synced creation ${item.id} to Firestore")
        } catch (e: Exception) {
            Log.w("GraffitiRepo", "Cloud upload failed: ${e.message}")
        }
    }

    suspend fun syncAllWithCloud(localList: List<SavedGraffiti>): Result<Int> = withContext(Dispatchers.IO) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return@withContext Result.failure(Exception("Please sign in with Google to sync"))
        val db = firestore
            ?: return@withContext Result.failure(Exception("Cloud Firestore is not initialized"))

        try {
            // 1. Upload all local items to cloud
            for (item in localList) {
                syncSingleToCloud(item)
            }

            // 2. Download any cloud items missing from local Room database
            val snapshot = db.collection("users")
                .document(uid)
                .collection("creations")
                .get()
                .await()

            var importedCount = 0
            val existingIds = localList.map { it.id }.toSet()
            for (doc in snapshot.documents) {
                val cloudId = doc.getString("id")?.toLongOrNull() ?: doc.id.toLongOrNull() ?: 0L
                if (cloudId !in existingIds && cloudId != 0L) {
                    val entity = SavedGraffiti(
                        id = cloudId,
                        title = doc.getString("title") ?: "Cloud Piece",
                        text = doc.getString("text") ?: "GRAFFITI",
                        fillColorIndex = (doc.getLong("fillColorIndex") ?: 0L).toInt(),
                        isGradient = doc.getBoolean("isGradient") ?: true,
                        gradientColorIndex = (doc.getLong("gradientColorIndex") ?: 0L).toInt(),
                        outlineColorIndex = (doc.getLong("outlineColorIndex") ?: 0L).toInt(),
                        wallIndex = (doc.getLong("wallIndex") ?: 0L).toInt(),
                        lean = doc.getBoolean("lean") ?: true,
                        splats = doc.getBoolean("splats") ?: true,
                        drips = doc.getBoolean("drips") ?: true,
                        highlights = doc.getBoolean("highlights") ?: true,
                        fontStyleName = doc.getString("fontStyle") ?: GraffitiFontStyle.SISTER_SPRAY.name,
                        outlineThickness = (doc.getDouble("outlineThickness") ?: 14.0).toFloat(),
                        shadowDepth = (doc.getDouble("shadowDepth") ?: 16.0).toFloat(),
                        splatSeed = (doc.getLong("splatSeed") ?: 42L).toInt(),
                        splatDensity = (doc.getLong("splatDensity") ?: 18L).toInt(),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                    graffitiDao.insertGraffiti(entity)
                    importedCount++
                }
            }

            Result.success(localList.size + importedCount)
        } catch (e: Exception) {
            Log.e("GraffitiRepo", "Sync error", e)
            Result.failure(e)
        }
    }
}
