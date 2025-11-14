package com.hfad.pet_scheduling.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Helper class for picking images from gallery or camera
 */
class ImagePicker(private val fragment: Fragment) {
    
    private var onImageSelected: ((Uri?) -> Unit)? = null
    
    private val cameraPermissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        }
    }
    
    private val galleryLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageSelected?.invoke(uri)
    }
    
    private val cameraLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            onImageSelected?.invoke(cameraImageUri)
        }
    }
    
    private var cameraImageUri: Uri? = null
    
    /**
     * Show image picker options (Camera or Gallery)
     */
    fun pickImage(onImageSelected: (Uri?) -> Unit) {
        this.onImageSelected = onImageSelected
        
        val options = arrayOf("Camera", "Gallery")
        androidx.appcompat.app.AlertDialog.Builder(fragment.requireContext())
            .setTitle("Select Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun checkCameraPermission() {
        val permission = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(permission)
            }
        }
    }
    
    private fun openCamera() {
        try {
            val cacheDir = fragment.requireContext().cacheDir
            val imageFile = java.io.File(cacheDir, "pet_photo_${System.currentTimeMillis()}.jpg")
            cameraImageUri = androidx.core.content.FileProvider.getUriForFile(
                fragment.requireContext(),
                "${fragment.requireContext().packageName}.fileprovider",
                imageFile
            )
            cameraLauncher.launch(cameraImageUri)
        } catch (e: Exception) {
            android.util.Log.e("ImagePicker", "Error opening camera", e)
            onImageSelected?.invoke(null)
        }
    }
    
    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }
}

