package com.hfad.pet_scheduling.ui.sharing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.R
import com.hfad.pet_scheduling.databinding.FragmentSharePetBinding
import com.hfad.pet_scheduling.utils.Constants
import kotlinx.coroutines.launch

class SharePetFragment : Fragment() {
    private var _binding: FragmentSharePetBinding? = null
    private val binding get() = _binding!!

    private var petId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSharePetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        petId = arguments?.getString("petId")
        if (petId == null) {
            Toast.makeText(requireContext(), "Pet ID not found", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        setupToolbar()
        setupPermissionToggle()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupPermissionToggle() {
        // Set default to VIEW permission
        binding.permissionToggleGroup.check(R.id.btnPermissionView)

        binding.permissionToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                // Permission selection handled
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnShare.setOnClickListener {
            sharePet()
        }
    }

    private fun sharePet() {
        val email = binding.etEmail.text?.toString()?.trim()
        if (email.isNullOrEmpty()) {
            binding.tilEmail.error = "Email is required"
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Get selected permission level
        val permissionLevel = when (binding.permissionToggleGroup.checkedButtonId) {
            R.id.btnPermissionView -> Constants.PermissionLevel.VIEW
            R.id.btnPermissionEdit -> Constants.PermissionLevel.EDIT
            R.id.btnPermissionManage -> Constants.PermissionLevel.MANAGE
            else -> Constants.PermissionLevel.VIEW
        }

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnShare.isEnabled = false

                val application = requireActivity().application as PetSchedulingApplication
                val syncService = com.hfad.pet_scheduling.data.remote.FirestoreSyncService()
                
                // Get pet to verify ownership
                val pet = application.petRepository.getPetByIdSuspend(petId!!)
                if (pet == null) {
                    Toast.makeText(requireContext(), "Pet not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (pet.userId != currentUser.uid) {
                    Toast.makeText(requireContext(), "You can only share pets you own", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val emailStr = email!!.trim().lowercase()
                val lookupResult = syncService.lookupUserIdByEmail(emailStr)
                val sharedWithUserId = lookupResult.getOrNull()
                
                if (sharedWithUserId == null) {
                    Toast.makeText(
                        requireContext(),
                        "User not found. They need to sign up for Pet Scheduling first using this email.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                
                if (sharedWithUserId == currentUser.uid) {
                    Toast.makeText(requireContext(), "You cannot share a pet with yourself", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val createResult = syncService.createSharedAccessInFirestore(
                    petId = petId!!,
                    sharedWithUserId = sharedWithUserId,
                    sharedWithEmail = emailStr,
                    permissionLevel = permissionLevel
                )
                
                createResult.fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Successfully shared ${pet.name} with $emailStr", Toast.LENGTH_SHORT).show()
                        application.cloudSyncManager.fullSync()
                        findNavController().popBackStack()
                    },
                    onFailure = { e ->
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("SharePetFragment", "Error sharing pet", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnShare.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

