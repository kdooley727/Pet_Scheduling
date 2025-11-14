package com.hfad.pet_scheduling.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.R
import com.hfad.pet_scheduling.databinding.FragmentStatisticsBinding
import com.hfad.pet_scheduling.utils.Constants
import com.hfad.pet_scheduling.utils.StatisticsCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        loadStatistics()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadStatistics() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    val application = requireActivity().application as PetSchedulingApplication

                    // Get all pets
                    val pets = application.petRepository.getAllPetsByUser(currentUser.uid).first()
                    
                    // Get all tasks (including inactive for complete statistics)
                    val allTasks = application.scheduleRepository.getAllActiveTasks()
                    
                    // Get all completed tasks
                    val allCompletedTasks = mutableListOf<com.hfad.pet_scheduling.data.local.entities.CompletedTask>()
                    pets.forEach { pet ->
                        val petCompletedTasks = application.scheduleRepository.getCompletedTasksByPet(pet.petId)
                        allCompletedTasks.addAll(petCompletedTasks)
                    }

                    // Calculate statistics
                    val taskStats = StatisticsCalculator.calculateTaskStatistics(allTasks, allCompletedTasks)
                    
                    // Display statistics
                    displayStatistics(taskStats, pets, application, allCompletedTasks)
                } catch (e: Exception) {
                    android.util.Log.e("StatisticsFragment", "Error loading statistics", e)
                }
            }
        }
    }

    private suspend fun displayStatistics(
        taskStats: com.hfad.pet_scheduling.data.TaskStatistics,
        pets: List<com.hfad.pet_scheduling.data.local.entities.Pet>,
        application: PetSchedulingApplication,
        allCompletedTasks: List<com.hfad.pet_scheduling.data.local.entities.CompletedTask>
    ) {
        // Update overview cards
        binding.tvTotalTasks.text = taskStats.totalTasks.toString()
        binding.tvCompletionRate.text = String.format("%.1f%%", taskStats.completionRate)
        binding.tvCurrentStreak.text = "${taskStats.currentStreak} days"
        binding.tvLongestStreak.text = "${taskStats.longestStreak} days"

        // Display tasks by category
        displayCategoryStats(taskStats)

        // Display pet statistics
        displayPetStats(pets, application, allCompletedTasks)

        // Display recent activity
        displayRecentActivity(taskStats.recentCompletions)
    }

    private fun displayCategoryStats(taskStats: com.hfad.pet_scheduling.data.TaskStatistics) {
        binding.categoryStatsContainer.removeAllViews()

        if (taskStats.tasksByCategory.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "No tasks yet"
                textSize = 14f
                val textColor = androidx.core.content.ContextCompat.getColorStateList(requireContext(), android.R.attr.textColorSecondary)?.defaultColor
                    ?: requireContext().getColor(android.R.color.darker_gray)
                setTextColor(textColor)
                setPadding(0, 8, 0, 8)
            }
            binding.categoryStatsContainer.addView(emptyText)
            return
        }

        taskStats.tasksByCategory.forEach { (category, count) ->
            val completed = taskStats.completionByCategory[category] ?: 0
            val completionRate = if (count > 0) {
                (completed.toFloat() / count.toFloat()) * 100f
            } else {
                0f
            }

            val categoryView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_category_stat, binding.categoryStatsContainer, false)

            val tvCategoryName = categoryView.findViewById<TextView>(R.id.tvCategoryName)
            val tvCategoryCount = categoryView.findViewById<TextView>(R.id.tvCategoryCount)
            val tvCategoryCompletion = categoryView.findViewById<TextView>(R.id.tvCategoryCompletion)
            val progressBar = categoryView.findViewById<android.widget.ProgressBar>(R.id.progressBar)

            tvCategoryName.text = Constants.TaskCategory.getDisplayName(category)
            tvCategoryCount.text = "$completed / $count"
            tvCategoryCompletion.text = String.format("%.0f%%", completionRate)
            progressBar.progress = completionRate.toInt()

            binding.categoryStatsContainer.addView(categoryView)
        }
    }

    private suspend fun displayPetStats(
        pets: List<com.hfad.pet_scheduling.data.local.entities.Pet>,
        application: PetSchedulingApplication,
        allCompletedTasks: List<com.hfad.pet_scheduling.data.local.entities.CompletedTask>
    ) {
        binding.petStatsContainer.removeAllViews()

        if (pets.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "No pets yet"
                textSize = 14f
                val textColor = androidx.core.content.ContextCompat.getColorStateList(requireContext(), android.R.attr.textColorSecondary)?.defaultColor
                    ?: requireContext().getColor(android.R.color.darker_gray)
                setTextColor(textColor)
                setPadding(0, 8, 0, 8)
            }
            binding.petStatsContainer.addView(emptyText)
            return
        }

        pets.forEach { pet ->
            val petTasks = application.scheduleRepository.getActiveTasksByPet(pet.petId).first()
            val petStats = StatisticsCalculator.calculatePetStatistics(
                pet.petId,
                pet.name,
                petTasks,
                allCompletedTasks
            )

            val petView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_pet_stat, binding.petStatsContainer, false)

            val tvPetName = petView.findViewById<TextView>(R.id.tvPetName)
            val tvPetTasks = petView.findViewById<TextView>(R.id.tvPetTasks)
            val tvPetCompletion = petView.findViewById<TextView>(R.id.tvPetCompletion)
            val progressBar = petView.findViewById<android.widget.ProgressBar>(R.id.progressBar)

            tvPetName.text = petStats.petName
            tvPetTasks.text = "${petStats.completedTasks} / ${petStats.totalTasks} tasks"
            tvPetCompletion.text = String.format("%.0f%%", petStats.completionRate)
            progressBar.progress = petStats.completionRate.toInt()

            binding.petStatsContainer.addView(petView)

            // Add divider except for last item
            if (pet != pets.last()) {
                val divider = View(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1
                    )
                    val dividerColor = androidx.core.content.ContextCompat.getColorStateList(requireContext(), android.R.attr.textColorSecondary)?.defaultColor
                        ?: requireContext().getColor(android.R.color.darker_gray)
                    setBackgroundColor(dividerColor)
                    alpha = 0.3f
                }
                val dividerParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1
                ).apply {
                    setMargins(0, 16, 0, 16)
                }
                divider.layoutParams = dividerParams
                binding.petStatsContainer.addView(divider)
            }
        }
    }

    private fun displayRecentActivity(recentCompletions: List<com.hfad.pet_scheduling.data.CompletionEntry>) {
        binding.recentActivityContainer.removeAllViews()

        if (recentCompletions.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "No recent activity"
                textSize = 14f
                val textColor = androidx.core.content.ContextCompat.getColorStateList(requireContext(), android.R.attr.textColorSecondary)?.defaultColor
                    ?: requireContext().getColor(android.R.color.darker_gray)
                setTextColor(textColor)
                setPadding(0, 8, 0, 8)
            }
            binding.recentActivityContainer.addView(emptyText)
            return
        }

        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        recentCompletions.forEach { entry ->
            val activityView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_recent_activity, binding.recentActivityContainer, false)

            val tvDate = activityView.findViewById<TextView>(R.id.tvDate)
            val tvDay = activityView.findViewById<TextView>(R.id.tvDay)
            val tvCount = activityView.findViewById<TextView>(R.id.tvCount)

            tvDate.text = dateFormat.format(Date(entry.date))
            tvDay.text = dayFormat.format(Date(entry.date))
            tvCount.text = "${entry.count} task${if (entry.count != 1) "s" else ""}"

            binding.recentActivityContainer.addView(activityView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

