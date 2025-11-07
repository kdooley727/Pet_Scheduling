package com.hfad.pet_scheduling.ui.pets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.R
import com.hfad.pet_scheduling.databinding.FragmentPetListBinding
import com.hfad.pet_scheduling.viewmodels.PetViewModel
import com.hfad.pet_scheduling.viewmodels.ViewModelFactory

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
            application.scheduleRepository
        )
        petViewModel = ViewModelProvider(this, factory)[PetViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        // Check authentication
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            petViewModel.initialize(currentUser.uid)
        } else {
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
                // Navigate to edit pet
                val bundle = Bundle().apply {
                    putString("petId", pet.petId)
                }
                findNavController().navigate(R.id.action_petListFragment_to_addEditPetFragment, bundle)
            }
        )

        binding.recyclerViewPets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = petAdapter
        }
    }

    private fun setupObservers() {
        petViewModel.pets.observe(viewLifecycleOwner) { pets ->
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
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        petViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

