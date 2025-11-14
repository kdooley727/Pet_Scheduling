package com.hfad.pet_scheduling.ui.pets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.R
import com.hfad.pet_scheduling.databinding.FragmentPetListBinding
import com.hfad.pet_scheduling.utils.CloudSyncManager
import com.hfad.pet_scheduling.viewmodels.PetViewModel
import com.hfad.pet_scheduling.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch

class PetListFragment : Fragment() {
    private var _binding: FragmentPetListBinding? = null
    private val binding get() = _binding!!

    private lateinit var petViewModel: PetViewModel
    private lateinit var petAdapter: PetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetListBinding.inflate(inflater, container, false)
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

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupSyncStatus()

        // Check authentication
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            android.util.Log.d("PetListFragment", "User authenticated: ${currentUser.uid}")
            petViewModel.initialize(currentUser.uid)
        } else {
            android.util.Log.w("PetListFragment", "No user authenticated, navigating to login")
            findNavController().navigate(R.id.action_petListFragment_to_loginFragment)
        }
    }

    private fun setupRecyclerView() {
        petAdapter = PetAdapter(
            onPetClick = { pet ->
                // Navigate to task list for this pet
                val bundle = Bundle().apply {
                    putString("petId", pet.petId)
                }
                findNavController().navigate(R.id.action_petListFragment_to_taskListFragment, bundle)
            },
            onPetLongClick = { pet ->
                // Show options menu (Edit, Delete)
                showPetOptionsMenu(pet)
            },
            onShareClick = { pet ->
                // Navigate to share pet screen
                val bundle = Bundle().apply {
                    putString("petId", pet.petId)
                }
                findNavController().navigate(R.id.action_petListFragment_to_sharePetFragment, bundle)
            }
        )

        binding.recyclerViewPets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = petAdapter
            // Add item animations
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator().apply {
                addDuration = 300
                removeDuration = 300
                moveDuration = 300
                changeDuration = 300
            }
        }
    }

    private fun setupObservers() {
        petViewModel.pets.observe(viewLifecycleOwner) { pets ->
            android.util.Log.d("PetListFragment", "Pets updated: ${pets.size} pets")
            if (pets.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerViewPets.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.recyclerViewPets.visibility = View.VISIBLE
                petAdapter.submitList(pets)
            }
        }

        petViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            android.util.Log.d("PetListFragment", "Loading state: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        petViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                android.util.Log.e("PetListFragment", "Error: $it")
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                petViewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddPet.setOnClickListener {
            findNavController().navigate(R.id.action_petListFragment_to_addEditPetFragment)
        }

        binding.btnSignOut.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            findNavController().navigate(R.id.action_petListFragment_to_loginFragment)
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_petListFragment_to_settingsFragment)
        }

        binding.btnStatistics.setOnClickListener {
            findNavController().navigate(R.id.action_petListFragment_to_statisticsFragment)
        }
    }

    private fun showPetOptionsMenu(pet: com.hfad.pet_scheduling.data.local.entities.Pet) {
        val popupMenu = android.widget.PopupMenu(requireContext(), binding.recyclerViewPets)
        popupMenu.menuInflater.inflate(R.menu.pet_options_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit_pet -> {
                    val bundle = Bundle().apply {
                        putString("petId", pet.petId)
                    }
                    findNavController().navigate(R.id.action_petListFragment_to_addEditPetFragment, bundle)
                    true
                }
                R.id.action_delete_pet -> {
                    showDeleteConfirmation(pet)
                    true
                }
                R.id.action_share_pet -> {
                    val bundle = Bundle().apply {
                        putString("petId", pet.petId)
                    }
                    findNavController().navigate(R.id.action_petListFragment_to_sharePetFragment, bundle)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showDeleteConfirmation(pet: com.hfad.pet_scheduling.data.local.entities.Pet) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Pet")
            .setMessage("Are you sure you want to delete ${pet.name}? This will also delete all associated tasks and cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                petViewModel.deletePet(pet)
                android.widget.Toast.makeText(
                    requireContext(),
                    "${pet.name} deleted",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupSyncStatus() {
        val application = requireActivity().application as PetSchedulingApplication
        val syncManager = application.cloudSyncManager

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                syncManager.syncStatus.collect { status ->
                    updateSyncStatusUI(status)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                syncManager.syncMessage.collect { message ->
                    binding.syncStatusView.syncStatusText.text = message ?: "Synced"
                }
            }
        }
    }

    private fun updateSyncStatusUI(status: CloudSyncManager.SyncStatus) {
        val syncView = binding.syncStatusView
        val context = requireContext()
        
        when (status) {
            CloudSyncManager.SyncStatus.IDLE -> {
                syncView.syncProgressBar.visibility = View.GONE
                syncView.syncIcon.visibility = View.VISIBLE
                syncView.syncIcon.setImageResource(android.R.drawable.ic_menu_upload)
                val idleColor = androidx.core.content.ContextCompat.getColor(context, R.color.sync_idle)
                syncView.syncIcon.setColorFilter(idleColor, android.graphics.PorterDuff.Mode.SRC_IN)
                syncView.syncStatusText.text = "Synced"
                syncView.syncStatusText.setTextColor(idleColor)
            }
            CloudSyncManager.SyncStatus.SYNCING -> {
                syncView.syncProgressBar.visibility = View.VISIBLE
                syncView.syncIcon.visibility = View.GONE
                syncView.syncStatusText.text = "Syncing..."
                val syncingColor = androidx.core.content.ContextCompat.getColor(context, R.color.sync_syncing)
                syncView.syncStatusText.setTextColor(syncingColor)
            }
            CloudSyncManager.SyncStatus.SUCCESS -> {
                syncView.syncProgressBar.visibility = View.GONE
                syncView.syncIcon.visibility = View.VISIBLE
                syncView.syncIcon.setImageResource(android.R.drawable.checkbox_on_background)
                val successColor = androidx.core.content.ContextCompat.getColor(context, R.color.sync_success)
                syncView.syncIcon.setColorFilter(successColor, android.graphics.PorterDuff.Mode.SRC_IN)
                syncView.syncStatusText.text = "Synced"
                syncView.syncStatusText.setTextColor(successColor)
            }
            CloudSyncManager.SyncStatus.ERROR -> {
                syncView.syncProgressBar.visibility = View.GONE
                syncView.syncIcon.visibility = View.VISIBLE
                syncView.syncIcon.setImageResource(android.R.drawable.ic_dialog_alert)
                val errorColor = androidx.core.content.ContextCompat.getColor(context, R.color.sync_error)
                syncView.syncIcon.setColorFilter(errorColor, android.graphics.PorterDuff.Mode.SRC_IN)
                syncView.syncStatusText.text = "Sync failed"
                syncView.syncStatusText.setTextColor(errorColor)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

