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

        // Note: In a real app, you'd need to look up the user by email in Firebase Auth
        // For now, we'll store the email and the actual user lookup would happen server-side
        // or when the shared user logs in

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnShare.isEnabled = false

                val application = requireActivity().application as PetSchedulingApplication
                
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

                // For now, we'll create a shared access record with the email
                // In production, you'd need to:
                // 1. Look up user by email in Firebase Auth
                // 2. Create SharedAccess with the actual userId
                // 3. Send notification/email to the user

                Toast.makeText(
                    requireContext(),
                    "Sharing functionality requires user lookup by email. This feature will be enhanced in future updates.",
                    Toast.LENGTH_LONG
                ).show()

                // TODO: Implement actual sharing when user lookup is available
                // For now, just show a message
                
                findNavController().popBackStack()
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

