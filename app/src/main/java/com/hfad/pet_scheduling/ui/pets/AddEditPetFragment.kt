package com.hfad.pet_scheduling.ui.pets

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.R
import com.hfad.pet_scheduling.data.local.entities.Pet
import com.hfad.pet_scheduling.databinding.FragmentAddEditPetBinding
import com.hfad.pet_scheduling.utils.Constants
import com.hfad.pet_scheduling.utils.DateTimeUtils
import com.hfad.pet_scheduling.utils.FirebaseStorageHelper
import com.hfad.pet_scheduling.utils.ImagePicker
import com.hfad.pet_scheduling.viewmodels.PetViewModel
import com.hfad.pet_scheduling.viewmodels.ViewModelFactory
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.*

class AddEditPetFragment : Fragment() {
    private var _binding: FragmentAddEditPetBinding? = null
    private val binding get() = _binding!!

    private lateinit var petViewModel: PetViewModel
    private var petId: String? = null
    private var selectedBirthDate: Long? = null
    private var isEditMode = false
    private var selectedImageUri: Uri? = null
    private lateinit var imagePicker: ImagePicker

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
            application.scheduleRepository,
            application
        )
        petViewModel = ViewModelProvider(this, factory)[PetViewModel::class.java]

        imagePicker = ImagePicker(this)

        setupObservers()
        setupClickListeners()
        setupSpinners()

        // Get petId from arguments
        petId = arguments?.getString("petId")

        // Check if editing existing pet
        petId?.let { id ->
            isEditMode = true
            binding.toolbar.title = "Edit Pet"
            android.util.Log.d("AddEditPetFragment", "Edit mode: petId=$id")
            petViewModel.getPetById(id)
        } ?: run {
            binding.toolbar.title = "Add Pet"
            android.util.Log.d("AddEditPetFragment", "Add mode: no petId")
        }
        
        // Set delete button visibility immediately based on edit mode
        binding.btnDelete.visibility = if (isEditMode) View.VISIBLE else View.GONE
        android.util.Log.d("AddEditPetFragment", "Delete button visibility set to: ${if (isEditMode) "VISIBLE" else "GONE"}")
    }

    private var isSaving = false
    private var isDeleting = false
    
    private fun setupObservers() {
        petViewModel.selectedPet.observe(viewLifecycleOwner) { pet ->
            pet?.let {
                populateFields(it)
                // Ensure delete button is visible when pet is loaded in edit mode
                if (isEditMode) {
                    binding.btnDelete.visibility = View.VISIBLE
                }
            }
        }

        petViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
            binding.btnDelete.isEnabled = !isLoading
            
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
            
            // If we were deleting and loading just finished, check if deletion was successful
            if (isDeleting && !isLoading && isAdded) {
                isDeleting = false
                val error = petViewModel.errorMessage.value
                if (error == null) {
                    // Deletion successful
                    android.util.Log.d("AddEditPetFragment", "Pet deleted successfully, navigating back")
                    try {
                        Toast.makeText(requireContext(), "Pet deleted successfully", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } catch (e: Exception) {
                        android.util.Log.e("AddEditPetFragment", "Error navigating after delete", e)
                    }
                } else {
                    android.util.Log.e("AddEditPetFragment", "Error deleting pet: $error")
                    Toast.makeText(requireContext(), "Error deleting pet: $error", Toast.LENGTH_LONG).show()
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
            val petTypeDisplayName = Constants.PetType.getDisplayName(pet.type)
            spinnerPetType.setText(petTypeDisplayName, false)
            etBreed.setText(pet.breed ?: "")
            pet.birthDate?.let {
                selectedBirthDate = it
                btnBirthDate.text = DateTimeUtils.formatDate(it)
            }
            etNotes.setText(pet.notes ?: "")
            
            // Load emergency contacts
            etVetName.setText(pet.vetName ?: "")
            etVetPhone.setText(pet.vetPhone ?: "")
            etVetEmail.setText(pet.vetEmail ?: "")
            etVetAddress.setText(pet.vetAddress ?: "")
            etEmergencyContactName.setText(pet.emergencyContactName ?: "")
            etEmergencyContactPhone.setText(pet.emergencyContactPhone ?: "")
            etEmergencyContactEmail.setText(pet.emergencyContactEmail ?: "")
            etEmergencyContactRelationship.setText(pet.emergencyContactRelationship ?: "")
            
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
        // Pet type dropdown (AutoCompleteTextView)
        val petTypes = Constants.PetType.ALL_TYPES.map { Constants.PetType.getDisplayName(it) }
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            petTypes
        )
        binding.spinnerPetType.setAdapter(adapter)
        
        // Set default selection if not in edit mode
        if (!isEditMode && petTypes.isNotEmpty()) {
            binding.spinnerPetType.setText(petTypes[0], false)
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnBirthDate.setOnClickListener {
            showDatePicker()
        }
        
        binding.btnSelectPhoto.setOnClickListener {
            imagePicker.pickImage { uri ->
                uri?.let {
                    selectedImageUri = it
                    Glide.with(this@AddEditPetFragment)
                        .load(it)
                        .circleCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(binding.ivPetPhoto)
                }
            }
        }

        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                savePet()
            }
        }

        // Setup delete button click listener
        binding.btnDelete.setOnClickListener {
            petViewModel.selectedPet.value?.let { pet ->
                showDeleteConfirmation(pet)
            } ?: run {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Pet data not loaded yet",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        // Show delete button only in edit mode
        if (isEditMode) {
            binding.btnDelete.visibility = View.VISIBLE
            android.util.Log.d("AddEditPetFragment", "Delete button should be visible - isEditMode=true")
        } else {
            binding.btnDelete.visibility = View.GONE
            android.util.Log.d("AddEditPetFragment", "Delete button hidden - isEditMode=false")
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
        
        // Get selected pet type from AutoCompleteTextView
        val selectedPetTypeText = binding.spinnerPetType.text.toString()
        val petType = Constants.PetType.ALL_TYPES.find { 
            Constants.PetType.getDisplayName(it) == selectedPetTypeText 
        } ?: Constants.PetType.OTHER // Default fallback
        val breed = binding.etBreed.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val notes = binding.etNotes.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        
        // Emergency contacts
        val vetName = binding.etVetName.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val vetPhone = binding.etVetPhone.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val vetEmail = binding.etVetEmail.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val vetAddress = binding.etVetAddress.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val emergencyContactName = binding.etEmergencyContactName.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val emergencyContactPhone = binding.etEmergencyContactPhone.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val emergencyContactEmail = binding.etEmergencyContactEmail.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val emergencyContactRelationship = binding.etEmergencyContactRelationship.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }

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
                    photoUrl = FirebaseStorageHelper.uploadPetPhoto(selectedImageUri!!, finalPetId)
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
                        vetName = vetName,
                        vetPhone = vetPhone,
                        vetEmail = vetEmail,
                        vetAddress = vetAddress,
                        emergencyContactName = emergencyContactName,
                        emergencyContactPhone = emergencyContactPhone,
                        emergencyContactEmail = emergencyContactEmail,
                        emergencyContactRelationship = emergencyContactRelationship,
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
                        vetName = vetName,
                        vetPhone = vetPhone,
                        vetEmail = vetEmail,
                        vetAddress = vetAddress,
                        emergencyContactName = emergencyContactName,
                        emergencyContactPhone = emergencyContactPhone,
                        emergencyContactEmail = emergencyContactEmail,
                        emergencyContactRelationship = emergencyContactRelationship,
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
    

    private fun showDeleteConfirmation(pet: Pet) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Pet")
            .setMessage("Are you sure you want to delete ${pet.name}? This will also delete all associated tasks and cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                android.util.Log.d("AddEditPetFragment", "Delete confirmed for pet: ${pet.name}")
                isDeleting = true
                petViewModel.deletePet(pet)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

