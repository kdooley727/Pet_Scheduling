package com.hfad.pet_scheduling.ui.sharing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.R
import com.hfad.pet_scheduling.databinding.FragmentManageSharedAccessBinding
import com.hfad.pet_scheduling.utils.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ManageSharedAccessFragment : Fragment() {
    private var _binding: FragmentManageSharedAccessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageSharedAccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupViewPager()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupViewPager() {
        val adapter = SharedAccessPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Shared With Me"
                1 -> "I Shared"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SharedAccessPagerAdapter(fragment: Fragment) : androidx.viewpager2.adapter.FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return SharedAccessListFragment.newInstance(position == 0)
    }
}

class SharedAccessListFragment : Fragment() {
    private var _binding: com.hfad.pet_scheduling.databinding.FragmentSharedAccessListBinding? = null
    private val binding get() = _binding!!

    private var isSharedWithMe: Boolean = false
    private lateinit var adapter: SharedAccessAdapter

    companion object {
        fun newInstance(isSharedWithMe: Boolean): SharedAccessListFragment {
            return SharedAccessListFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isSharedWithMe", isSharedWithMe)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = com.hfad.pet_scheduling.databinding.FragmentSharedAccessListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isSharedWithMe = arguments?.getBoolean("isSharedWithMe") ?: false

        adapter = SharedAccessAdapter(
            isSharedWithMe = isSharedWithMe,
            onActionClick = { sharedAccess, petName ->
                showActionMenu(sharedAccess, petName)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        loadSharedAccess()
    }

    private fun loadSharedAccess() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val application = requireActivity().application as PetSchedulingApplication
                
                if (isSharedWithMe) {
                    // Load pets shared with current user
                    val sharedAccessList = application.petRepository.getSharedPetsForUser(currentUser.uid).first()
                    val sharedAccessWithPets = sharedAccessList.mapNotNull { sharedAccess ->
                        val pet = application.petRepository.getPetByIdSuspend(sharedAccess.petId)
                        pet?.let { Pair(sharedAccess, it) }
                    }
                    adapter.submitList(sharedAccessWithPets)
                } else {
                    // Load pets shared by current user
                    val sharedAccessList = application.petRepository.getPetsSharedByUser(currentUser.uid).first()
                    val sharedAccessWithPets = sharedAccessList.mapNotNull { sharedAccess ->
                        val pet = application.petRepository.getPetByIdSuspend(sharedAccess.petId)
                        pet?.let { Pair(sharedAccess, it) }
                    }
                    adapter.submitList(sharedAccessWithPets)
                }

                binding.tvEmpty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                android.util.Log.e("SharedAccessListFragment", "Error loading shared access", e)
                Toast.makeText(requireContext(), "Error loading shared access", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showActionMenu(sharedAccess: com.hfad.pet_scheduling.data.local.entities.SharedAccess, petName: String) {
        val popup = PopupMenu(requireContext(), binding.recyclerView)
        popup.menuInflater.inflate(R.menu.shared_access_menu, popup.menu)

        if (isSharedWithMe) {
            // Can only revoke access if shared with me
            popup.menu.findItem(R.id.action_revoke).isVisible = false
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_change_permission -> {
                    // TODO: Implement change permission
                    Toast.makeText(requireContext(), "Change permission - Coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_revoke -> {
                    revokeAccess(sharedAccess, petName)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun revokeAccess(sharedAccess: com.hfad.pet_scheduling.data.local.entities.SharedAccess, petName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val application = requireActivity().application as PetSchedulingApplication
                application.petRepository.revokeSharedAccess(sharedAccess.shareId)
                Toast.makeText(requireContext(), "Access revoked for $petName", Toast.LENGTH_SHORT).show()
                loadSharedAccess()
            } catch (e: Exception) {
                android.util.Log.e("SharedAccessListFragment", "Error revoking access", e)
                Toast.makeText(requireContext(), "Error revoking access", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SharedAccessAdapter(
    private val isSharedWithMe: Boolean,
    private val onActionClick: (com.hfad.pet_scheduling.data.local.entities.SharedAccess, String) -> Unit
) : androidx.recyclerview.widget.ListAdapter<Pair<com.hfad.pet_scheduling.data.local.entities.SharedAccess, com.hfad.pet_scheduling.data.local.entities.Pet>, SharedAccessAdapter.ViewHolder>(
    SharedAccessDiffCallback()
) {
    
    class SharedAccessDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Pair<com.hfad.pet_scheduling.data.local.entities.SharedAccess, com.hfad.pet_scheduling.data.local.entities.Pet>>() {
        override fun areItemsTheSame(
            oldItem: Pair<com.hfad.pet_scheduling.data.local.entities.SharedAccess, com.hfad.pet_scheduling.data.local.entities.Pet>,
            newItem: Pair<com.hfad.pet_scheduling.data.local.entities.SharedAccess, com.hfad.pet_scheduling.data.local.entities.Pet>
        ): Boolean {
            return oldItem.first.shareId == newItem.first.shareId
        }

        override fun areContentsTheSame(
            oldItem: Pair<com.hfad.pet_scheduling.data.local.entities.SharedAccess, com.hfad.pet_scheduling.data.local.entities.Pet>,
            newItem: Pair<com.hfad.pet_scheduling.data.local.entities.SharedAccess, com.hfad.pet_scheduling.data.local.entities.Pet>
        ): Boolean {
            return oldItem == newItem
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = com.hfad.pet_scheduling.databinding.ItemSharedAccessBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: com.hfad.pet_scheduling.databinding.ItemSharedAccessBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Pair<com.hfad.pet_scheduling.data.local.entities.SharedAccess, com.hfad.pet_scheduling.data.local.entities.Pet>) {
            val (sharedAccess, pet) = item
            binding.tvPetName.text = pet.name
            binding.tvPermission.text = Constants.PermissionLevel.getDisplayName(sharedAccess.permissionLevel)
            
            // TODO: Get user email/name from Firebase Auth
            binding.tvUserInfo.text = if (isSharedWithMe) {
                "Shared by: ${sharedAccess.ownerUserId}"
            } else {
                "Shared with: ${sharedAccess.sharedWithUserId}"
            }

            binding.btnAction.setOnClickListener {
                onActionClick(sharedAccess, pet.name)
            }
        }
    }
}

