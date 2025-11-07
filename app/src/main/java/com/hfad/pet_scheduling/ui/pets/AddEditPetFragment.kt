package com.hfad.pet_scheduling.ui.pets

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.R
import com.hfad.pet_scheduling.data.local.entities.Pet
import com.hfad.pet_scheduling.databinding.FragmentAddEditPetBinding
import com.hfad.pet_scheduling.utils.Constants
import com.hfad.pet_scheduling.utils.DateTimeUtils
import com.hfad.pet_scheduling.viewmodels.PetViewModel
import com.hfad.pet_scheduling.viewmodels.ViewModelFactory
import java.util.*

class AddEditPetFragment : Fragment() {
    private var _binding: FragmentAddEditPetBinding? = null
    private val binding get() = _binding!!

    private lateinit var petViewModel: PetViewModel
    private var petId: String? = null
    private var selectedBirthDate: Long? = null
    private var isEditMode = false

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

    private fun setupObservers() {
        petViewModel.selectedPet.observe(viewLifecycleOwner) { pet ->
            pet?.let {
                populateFields(it)
            }
        }

        petViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }

        petViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                petViewModel.clearError()
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
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val petName = binding.etPetName.text.toString().trim()
        val petType = Constants.PetType.ALL_TYPES[binding.spinnerPetType.selectedItemPosition]
        val breed = binding.etBreed.text.toString().trim().takeIf { it.isNotEmpty() }
        val notes = binding.etNotes.text.toString().trim().takeIf { it.isNotEmpty() }

        val pet = if (isEditMode && petId != null) {
            // Update existing pet
            petViewModel.selectedPet.value?.copy(
                name = petName,
                type = petType,
                breed = breed,
                birthDate = selectedBirthDate,
                notes = notes,
                updatedAt = System.currentTimeMillis()
            ) ?: return
        } else {
            // Create new pet
            Pet(
                userId = currentUser.uid,
                name = petName,
                type = petType,
                breed = breed,
                birthDate = selectedBirthDate,
                photoUrl = null,
                notes = notes
            )
        }

        petViewModel.savePet(pet)
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

