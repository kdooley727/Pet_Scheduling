package com.hfad.pet_scheduling.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.hfad.pet_scheduling.data.entities.CompletedTask
import com.hfad.pet_scheduling.data.entities.Pet
import com.hfad.pet_scheduling.data.entities.ScheduleTask
import kotlinx.coroutines.tasks.await

/**
 * Service for syncing local Room database with Firebase Firestore
 */
class FirestoreSyncService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "FirestoreSyncService"
        private const val COLLECTION_PETS = "pets"
        private const val COLLECTION_TASKS = "tasks"
        private const val COLLECTION_COMPLETED_TASKS = "completed_tasks"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_SHARED_ACCESS = "shared_access"
    }

    /**
     * Get current user ID, return null if not authenticated
     */
    private fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Sync all pets to Firestore
     */
    suspend fun syncPetsToCloud(pets: List<Pet>): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            val batch = firestore.batch()
            
            pets.forEach { pet ->
                if (pet.userId != userId) {
                    Log.w(TAG, "Skipping pet ${pet.petId} - belongs to different user")
                    return@forEach
                }
                
                val petRef = firestore.collection(COLLECTION_PETS).document(pet.petId)
                val petData = pet.toFirestoreMap()
                batch.set(petRef, petData, SetOptions.merge())
            }
            
            batch.commit().await()
            Log.d(TAG, "✅ Synced ${pets.size} pets to Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error syncing pets to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Sync all tasks to Firestore
     */
    suspend fun syncTasksToCloud(tasks: List<ScheduleTask>): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            val batch = firestore.batch()
            
            tasks.forEach { task ->
                if (task.createdByUserId != userId) {
                    Log.w(TAG, "Skipping task ${task.taskId} - belongs to different user")
                    return@forEach
                }
                
                val taskRef = firestore.collection(COLLECTION_TASKS).document(task.taskId)
                val taskData = task.toFirestoreMap()
                batch.set(taskRef, taskData, SetOptions.merge())
            }
            
            batch.commit().await()
            Log.d(TAG, "✅ Synced ${tasks.size} tasks to Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error syncing tasks to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Sync completed tasks to Firestore
     */
    suspend fun syncCompletedTasksToCloud(completedTasks: List<CompletedTask>): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            val batch = firestore.batch()
            
            completedTasks.forEach { completedTask ->
                if (completedTask.completedByUserId != userId) {
                    Log.w(TAG, "Skipping completed task ${completedTask.completedTaskId} - belongs to different user")
                    return@forEach
                }
                
                val completedRef = firestore.collection(COLLECTION_COMPLETED_TASKS)
                    .document(completedTask.completedTaskId)
                val completedData = completedTask.toFirestoreMap()
                batch.set(completedRef, completedData, SetOptions.merge())
            }
            
            batch.commit().await()
            Log.d(TAG, "✅ Synced ${completedTasks.size} completed tasks to Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error syncing completed tasks to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch all pets from Firestore for current user
     */
    suspend fun fetchPetsFromCloud(): Result<List<Pet>> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            val snapshot = firestore.collection(COLLECTION_PETS)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val pets = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toPet()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing pet document ${doc.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "✅ Fetched ${pets.size} pets from Firestore")
            Result.success(pets)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching pets from Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch all tasks from Firestore for current user
     */
    suspend fun fetchTasksFromCloud(): Result<List<ScheduleTask>> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            val snapshot = firestore.collection(COLLECTION_TASKS)
                .whereEqualTo("createdByUserId", userId)
                .get()
                .await()
            
            val tasks = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toScheduleTask()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing task document ${doc.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "✅ Fetched ${tasks.size} tasks from Firestore")
            Result.success(tasks)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching tasks from Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch all completed tasks from Firestore for current user
     */
    suspend fun fetchCompletedTasksFromCloud(): Result<List<CompletedTask>> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            val snapshot = firestore.collection(COLLECTION_COMPLETED_TASKS)
                .whereEqualTo("completedByUserId", userId)
                .get()
                .await()
            
            val completedTasks = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toCompletedTask()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing completed task document ${doc.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "✅ Fetched ${completedTasks.size} completed tasks from Firestore")
            Result.success(completedTasks)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching completed tasks from Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Delete pet from Firestore
     */
    suspend fun deletePetFromCloud(petId: String): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            val petRef = firestore.collection(COLLECTION_PETS).document(petId)
            val petDoc = petRef.get().await()
            
            // Verify pet belongs to user
            if (petDoc.exists() && petDoc.getString("userId") != userId) {
                return Result.failure(Exception("Pet does not belong to user"))
            }
            
            petRef.delete().await()
            Log.d(TAG, "✅ Deleted pet $petId from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting pet from Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Save user profile for email lookup when sharing
     */
    suspend fun saveUserProfile(email: String, displayName: String?): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        return try {
            val data = mapOf(
                "email" to email.lowercase().trim(),
                "displayName" to (displayName ?: ""),
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_USERS).document(userId).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Look up user ID by email
     */
    suspend fun lookupUserIdByEmail(email: String): Result<String?> {
        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .whereEqualTo("email", email.lowercase().trim())
                .limit(1)
                .get()
                .await()
            val userId = snapshot.documents.firstOrNull()?.id
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create shared access in Firestore
     */
    suspend fun createSharedAccessInFirestore(
        petId: String,
        sharedWithUserId: String,
        sharedWithEmail: String,
        permissionLevel: String
    ): Result<Unit> {
        val ownerUserId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        val ownerEmail = auth.currentUser?.email ?: ""
        return try {
            val shareId = java.util.UUID.randomUUID().toString()
            val data = mapOf(
                "shareId" to shareId,
                "petId" to petId,
                "ownerUserId" to ownerUserId,
                "ownerEmail" to ownerEmail,
                "sharedWithUserId" to sharedWithUserId,
                "sharedWithEmail" to sharedWithEmail,
                "permissionLevel" to permissionLevel,
                "createdAt" to System.currentTimeMillis(),
                "isActive" to true
            )
            firestore.collection(COLLECTION_SHARED_ACCESS).document(shareId).set(data).await()
            Log.d(TAG, "✅ Created shared access for pet $petId with $sharedWithEmail")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create shared access", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch shared pets for current user from Firestore
     */
    suspend fun fetchSharedPetsFromCloud(): Result<List<Pair<com.hfad.pet_scheduling.data.entities.SharedAccess, Pet>>> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        return try {
            val snapshot = firestore.collection(COLLECTION_SHARED_ACCESS)
                .whereEqualTo("sharedWithUserId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            val result = mutableListOf<Pair<com.hfad.pet_scheduling.data.entities.SharedAccess, Pet>>()
            for (doc in snapshot.documents) {
                val shared = doc.toSharedAccess()
                val petDoc = firestore.collection(COLLECTION_PETS).document(shared.petId).get().await()
                if (petDoc.exists()) {
                    val pet = petDoc.toPet()
                    result.add(shared to pet)
                }
            }
            Log.d(TAG, "✅ Fetched ${result.size} shared pets from Firestore")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to fetch shared pets", e)
            Result.failure(e)
        }
    }

    /**
     * Delete task from Firestore
     */
    suspend fun deleteTaskFromCloud(taskId: String): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            val taskRef = firestore.collection(COLLECTION_TASKS).document(taskId)
            val taskDoc = taskRef.get().await()
            
            // Verify task belongs to user
            if (taskDoc.exists() && taskDoc.getString("createdByUserId") != userId) {
                return Result.failure(Exception("Task does not belong to user"))
            }
            
            taskRef.delete().await()
            Log.d(TAG, "✅ Deleted task $taskId from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting task from Firestore", e)
            Result.failure(e)
        }
    }
}

/**
 * Extension functions to convert entities to/from Firestore maps
 */

fun Pet.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "petId" to petId,
        "userId" to userId,
        "name" to name,
        "type" to type,
        "breed" to breed,
        "birthDate" to birthDate,
        "photoUrl" to photoUrl,
        "notes" to notes,
        "vetName" to vetName,
        "vetPhone" to vetPhone,
        "vetEmail" to vetEmail,
        "vetAddress" to vetAddress,
        "emergencyContactName" to emergencyContactName,
        "emergencyContactPhone" to emergencyContactPhone,
        "emergencyContactEmail" to emergencyContactEmail,
        "emergencyContactRelationship" to emergencyContactRelationship,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}

fun com.google.firebase.firestore.DocumentSnapshot.toPet(): Pet {
    return Pet(
        petId = getString("petId") ?: id,
        userId = getString("userId") ?: "",
        name = getString("name") ?: "",
        type = getString("type") ?: "",
        breed = getString("breed"),
        birthDate = getLong("birthDate"),
        photoUrl = getString("photoUrl"),
        notes = getString("notes"),
        vetName = getString("vetName"),
        vetPhone = getString("vetPhone"),
        vetEmail = getString("vetEmail"),
        vetAddress = getString("vetAddress"),
        emergencyContactName = getString("emergencyContactName"),
        emergencyContactPhone = getString("emergencyContactPhone"),
        emergencyContactEmail = getString("emergencyContactEmail"),
        emergencyContactRelationship = getString("emergencyContactRelationship"),
        createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
        updatedAt = getLong("updatedAt") ?: System.currentTimeMillis()
    )
}

fun ScheduleTask.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "taskId" to taskId,
        "petId" to petId,
        "title" to title,
        "description" to description,
        "category" to category,
        "startTime" to startTime,
        "recurrencePattern" to recurrencePattern,
        "recurrenceInterval" to recurrenceInterval,
        "reminderMinutesBefore" to reminderMinutesBefore,
        "isActive" to isActive,
        "createdAt" to createdAt,
        "createdByUserId" to createdByUserId
    )
}

fun com.google.firebase.firestore.DocumentSnapshot.toScheduleTask(): ScheduleTask {
    return ScheduleTask(
        taskId = getString("taskId") ?: id,
        petId = getString("petId") ?: "",
        title = getString("title") ?: "",
        description = getString("description"),
        category = getString("category") ?: "",
        startTime = getLong("startTime") ?: System.currentTimeMillis(),
        recurrencePattern = getString("recurrencePattern") ?: "",
        recurrenceInterval = (getLong("recurrenceInterval") ?: 1).toInt(),
        reminderMinutesBefore = (getLong("reminderMinutesBefore") ?: 15).toInt(),
        isActive = getBoolean("isActive") ?: true,
        createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
        createdByUserId = getString("createdByUserId") ?: ""
    )
}

fun CompletedTask.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "completedTaskId" to completedTaskId,
        "taskId" to taskId,
        "completedAt" to completedAt,
        "completedByUserId" to completedByUserId,
        "notes" to notes,
        "scheduledTime" to scheduledTime
    )
}

fun com.google.firebase.firestore.DocumentSnapshot.toSharedAccess(): com.hfad.pet_scheduling.data.entities.SharedAccess {
    return com.hfad.pet_scheduling.data.entities.SharedAccess(
        shareId = getString("shareId") ?: id,
        petId = getString("petId") ?: "",
        ownerUserId = getString("ownerUserId") ?: "",
        sharedWithUserId = getString("sharedWithUserId") ?: "",
        permissionLevel = getString("permissionLevel") ?: "view",
        createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
        isActive = getBoolean("isActive") ?: true
    )
}

fun com.google.firebase.firestore.DocumentSnapshot.toCompletedTask(): CompletedTask {
    return CompletedTask(
        completedTaskId = getString("completedTaskId") ?: id,
        taskId = getString("taskId") ?: "",
        completedAt = getLong("completedAt") ?: System.currentTimeMillis(),
        completedByUserId = getString("completedByUserId") ?: "",
        notes = getString("notes"),
        scheduledTime = getLong("scheduledTime")
    )
}

