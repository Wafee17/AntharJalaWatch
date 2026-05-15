package com.example.anthar_jalawatch.data.repository

import com.example.anthar_jalawatch.data.models.Borewell
import com.example.anthar_jalawatch.data.models.BorewellStatus
import com.example.anthar_jalawatch.util.WaterClusterCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BorewellRepository {
    private val db = FirebaseFirestore.getInstance()
    private val borewellsCollection = db.collection("borewells")
    private val auth = FirebaseAuth.getInstance()

    suspend fun addBorewellReading(
        depth: Double,
        yield: Double,
        yearOfDigging: Int,
        latitude: Double,
        longitude: Double
    ): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("You must be logged in to log a reading"))

            val documentRef = borewellsCollection.document()
            val anonymizedLat = String.format("%.3f", latitude).toDouble()
            val anonymizedLng = String.format("%.3f", longitude).toDouble()

            val status = WaterClusterCalculator.stressFromYield(yield)

            val newBorewell = Borewell(
                id = documentRef.id,
                userId = userId,
                latitude = anonymizedLat,
                longitude = anonymizedLng,
                depth = depth,
                yield = yield,
                yearOfDigging = yearOfDigging,
                status = status.name
            )

            documentRef.set(newBorewell).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBorewells(): Result<List<Borewell>> {
        return try {
            val snapshot = borewellsCollection.get().await()
            val borewells = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Borewell::class.java)?.copy(id = doc.id)
            }
            Result.success(borewells)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
