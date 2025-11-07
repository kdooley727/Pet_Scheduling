package com.hfad.pet_scheduling.ui.pets

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.R
import com.hfad.pet_scheduling.data.local.entities.Pet
import com.hfad.pet_scheduling.databinding.FragmentAddEditPetBinding
import com.hfad.pet_scheduling.utils.Constants
import com.hfad.pet_scheduling.utils.DateTimeUtils
import com.hfad.pet_scheduling.viewmodels.PetViewModel
import com.hfad.pet_scheduling.viewmodels.ViewModelFactory
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.util.*

class AddEditPetFragment : Fragment() {
    private var _binding: FragmentAddEditPetBinding? = null
    private val binding get() = _binding!!

    private lateinit var petViewModel: PetViewModel
    private var petId: String? = null
    private var selectedBirthDate: Long? = null
    private var isEditMode = false
    private var selectedImageUri: Uri? = null
    private val storage = FirebaseStorage.getInstance()
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(binding.ivPetPhoto)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditPetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val application = requireActivity().application as PetSchedulingApplication
        val factory = ViewModelFactory(
            application.petRepository,
            application.scheduleRepository
        )
        petViewModel = ViewModelProvider(this, factory)[PetViewModel::class.java]

        setupObservers()
        setupClickListeners()
        setupSpinners()

        // Get petId from arguments
        petId = arguments?.getString("petId")

        // Check if editing existing pet
        petId?.let { id ->
            isEditMode = true
            binding.toolbar.title = "Edit Pet"
            petViewModel.getPetById(id)
        } ?: run {
            binding.toolbar.title = "Add Pet"
        }
    }

    private var isSaving = false
    
    private fun setupObservers() {
        petViewModel.selectedPet.observe(viewLifecycleOwner) { pet ->
            pet?.let {
                populateFields(it)
            }
        }

        petViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
            
            // If we were saving and loading just finished, check if save was successful
            if (isSaving && !isLoading && isAdded) {
                isSaving = false
                val error = petViewModel.errorMessage.value
                if (error == null) {
                    // Save successful
                    try {
                        Toast.makeText(requireContext(), "Pet saved successfully", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } catch (e: Exception) {
                        android.util.Log.e("AddEditPetFragment", "Error navigating after save", e)
                    }
                }
            }
        }

        petViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                petViewModel.clearError()
                isSaving = false // Reset saving flag on error
            }
        }
    }

    private fun populateFields(pet: Pet) {
        binding.apply {
            etPetName.setText(pet.name)
            spinnerPetType.setSelection(
                Constants.PetType.ALL_TYPES.indexOf(pet.type).takeIf { it >= 0 } ?: 0
            )
            etBreed.setText(pet.breed ?: "")
            pet.birthDate?.let {
                selectedBirthDate = it
                btnBirthDate.text = DateTimeUtils.formatDate(it)
            }
            etNotes.setText(pet.notes ?: "")
            
            // Load pet photo if available
            pet.photoUrl?.let { photoUrl ->
                if (photoUrl.isNotEmpty()) {
                    Glide.with(this@AddEditPetFragment)
                        .load(photoUrl)
                        .circleCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(ivPetPhoto)
                }
            }
        }
    }

    private fun setupSpinners() {
        // Pet type spinner
        val petTypes = Constants.PetType.ALL_TYPES.map { Constants.PetType.getDisplayName(it) }
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            petTypes
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerPetType.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnBirthDate.setOnClickListener {
            showDatePicker()
        }
        
        binding.btnSelectPhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                savePet()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        selectedBirthDate?.let {
            calendar.timeInMillis = it
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedBirthDate = calendar.timeInMillis
                binding.btnBirthDate.text = DateTimeUtils.formatDate(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun validateInput(): Boolean {
        var isValid = true

        if (binding.etPetName.text.toString().trim().isEmpty()) {
            binding.etPetName.error = "Pet name is required"
            isValid = false
        }

        return isValid
    }

    private fun savePet() {
        // Safety check
        if (!isAdded || _binding == null) {
            android.util.Log.w("AddEditPetFragment", "Fragment not attached or binding is null")
            return
        }
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val petName = binding.etPetName.text?.toString()?.trim() ?: ""
        if (petName.isEmpty()) {
            binding.etPetName.error = "Pet name is required"
            return
        }
        
        val selectedPosition = binding.spinnerPetType.selectedItemPosition
        val petType = if (selectedPosition >= 0 && selectedPosition < Constants.PetType.ALL_TYPES.size) {
            Constants.PetType.ALL_TYPES[selectedPosition]
        } else {
            Constants.PetType.OTHER // Default fallback
        }
        val breed = binding.etBreed.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val notes = binding.etNotes.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }

        // Save pet
        lifecycleScope.launch {
            try {
                android.util.Log.d("AddEditPetFragment", "Starting save: name=$petName, type=$petType, userId=${currentUser.uid}")
                isSaving = true
                
                // Get or create pet ID
                val finalPetId = petId ?: UUID.randomUUID().toString()
                
                // Upload photo if selected
                var photoUrl: String? = null
                if (selectedImageUri != null) {
                    photoUrl = uploadPhoto(selectedImageUri!!, finalPetId)
                    // If upload failed, still allow saving without photo
                    if (photoUrl == null) {
                        Toast.makeText(
                            requireContext(),
                            "Saving pet without photo. You can add a photo later.",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Keep existing photo if editing, otherwise null
                        photoUrl = if (isEditMode) petViewModel.selectedPet.value?.photoUrl else null
                    }
                } else if (isEditMode) {
                    // Keep existing photo URL
                    photoUrl = petViewModel.selectedPet.value?.photoUrl
                }
                
                val pet = if (isEditMode && petId != null) {
                    // Update existing pet
                    petViewModel.selectedPet.value?.copy(
                        name = petName,
                        type = petType,
                        breed = breed,
                        birthDate = selectedBirthDate,
                        photoUrl = photoUrl,
                        notes = notes,
                        updatedAt = System.currentTimeMillis()
                    ) ?: return@launch
                } else {
                    // Create new pet - explicitly set all required fields
                    Pet(
                        petId = finalPetId,
                        userId = currentUser.uid,
                        name = petName,
                        type = petType,
                        breed = breed,
                        birthDate = selectedBirthDate,
                        photoUrl = photoUrl,
                        notes = notes,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                }
                
                petViewModel.savePet(pet, isNewPet = !isEditMode)
                // Navigation will happen automatically when loading finishes (handled in observer)
            } catch (e: Exception) {
                android.util.Log.e("AddEditPetFragment", "Error in savePet()", e)
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error saving pet: ${e.message}", Toast.LENGTH_LONG).show()
                isSaving = false
            }
        }
    }
    
    private suspend fun uploadPhoto(uri: Uri, petId: String): String? {
        return try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return null
            val storageRef = storage.reference
            val photoRef = storageRef.child("pet_photos/$userId/$petId.jpg")
            
            // Upload with metadata
            val uploadTask = photoRef.putFile(uri)
            uploadTask.await()
            
            // Get download URL
            val downloadUrl = photoRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: com.google.firebase.storage.StorageException) {
            android.util.Log.e("AddEditPetFragment", "Firebase Storage error", e)
            val errorCode = e.errorCode
            when {
                errorCode == -13010 || e.message?.contains("404") == true || e.message?.contains("Not Found") == true -> {
                    Toast.makeText(
                        requireContext(),
                        "Firebase Storage not configured. Please enable it in Firebase Console.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                errorCode == -13020 || e.message?.contains("unauthorized") == true || e.message?.contains("permission") == true -> {
                    Toast.makeText(
                        requireContext(),
                        "Storage permission denied. Check Firebase Storage rules.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    Toast.makeText(
                        requireContext(),
                        "Error uploading photo: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("AddEditPetFragment", "Error uploading photo", e)
            Toast.makeText(
                requireContext(),
                "Error uploading photo: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

