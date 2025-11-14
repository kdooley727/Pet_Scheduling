package com.hfad.pet_scheduling.utils

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Helper class for uploading images to Firebase Storage
 */
object FirebaseStorageHelper {
    private const val TAG = "FirebaseStorageHelper"
    private val storage = FirebaseStorage.getInstance()
    
    /**
     * Upload pet photo to Firebase Storage
     * @param imageUri Local URI of the image to upload
     * @param petId ID of the pet (for organizing storage)
     * @return Download URL of the uploaded image, or null if upload failed
     */
    suspend fun uploadPetPhoto(imageUri: Uri, petId: String): String? {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.e(TAG, "User not authenticated")
                return null
            }
            
            // Create unique filename
            val filename = "pet_${petId}_${UUID.randomUUID()}.jpg"
            val storagePath = "pet_photos/${currentUser.uid}/$filename"
            
            val storageRef = storage.reference.child(storagePath)
            
            // Upload the image
            val uploadTask = storageRef.putFile(imageUri).await()
            
            // Get download URL
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            val url = downloadUrl.toString()
            
            Log.d(TAG, "Photo uploaded successfully: $url")
            url
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading photo", e)
            null
        }
    }
    
    /**
     * Delete pet photo from Firebase Storage
     * @param photoUrl URL of the photo to delete
     */
    suspend fun deletePetPhoto(photoUrl: String) {
        try {
            if (photoUrl.isNotEmpty()) {
                val storageRef = storage.getReferenceFromUrl(photoUrl)
                storageRef.delete().await()
                Log.d(TAG, "Photo deleted successfully: $photoUrl")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting photo", e)
            // Don't throw - deletion failure shouldn't block other operations
        }
    }
}

