package com.hfad.pet_scheduling.ui.schedules

import android.app.AlertDialog
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
import com.hfad.pet_scheduling.databinding.FragmentTaskDetailBinding
import com.hfad.pet_scheduling.utils.Constants
import com.hfad.pet_scheduling.utils.DateTimeUtils
import com.hfad.pet_scheduling.viewmodels.ScheduleViewModel
import com.hfad.pet_scheduling.viewmodels.ViewModelFactory

class TaskDetailFragment : Fragment() {
    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var completedTaskAdapter: CompletedTaskAdapter
    private var taskId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskId = arguments?.getString("taskId")
        if (taskId == null) {
            Toast.makeText(requireContext(), "Task ID is required", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        val application = requireActivity().application as PetSchedulingApplication
        val factory = ViewModelFactory(
            application.petRepository,
            application.scheduleRepository
        )
        scheduleViewModel = ViewModelProvider(this, factory)[ScheduleViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        // Load task and completion history
        scheduleViewModel.getTaskById(taskId!!)
        scheduleViewModel.loadCompletedTasksForTask(taskId!!)
    }

    private fun setupRecyclerView() {
        completedTaskAdapter = CompletedTaskAdapter()

        binding.recyclerViewCompletions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = completedTaskAdapter
        }
    }

    private fun setupObservers() {
        scheduleViewModel.selectedTask.observe(viewLifecycleOwner) { task ->
            task?.let {
                populateTaskDetails(it)
            }
        }

        scheduleViewModel.completedTasks.observe(viewLifecycleOwner) { completedTasks ->
            if (completedTasks.isEmpty()) {
                binding.emptyCompletions.visibility = View.VISIBLE
                binding.recyclerViewCompletions.visibility = View.GONE
            } else {
                binding.emptyCompletions.visibility = View.GONE
                binding.recyclerViewCompletions.visibility = View.VISIBLE
                completedTaskAdapter.submitList(completedTasks)
            }
        }

        scheduleViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        scheduleViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                scheduleViewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnMarkComplete.setOnClickListener {
            showMarkCompleteDialog()
        }

        binding.btnEditTask.setOnClickListener {
            val bundle = Bundle().apply {
                putString("taskId", taskId)
            }
            findNavController().navigate(R.id.action_taskDetailFragment_to_addEditTaskFragment, bundle)
        }

        binding.btnDeleteTask.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun populateTaskDetails(task: com.hfad.pet_scheduling.data.local.entities.ScheduleTask) {
        binding.apply {
            tvTaskTitle.text = task.title
            tvTaskCategory.text = Constants.TaskCategory.getDisplayName(task.category)

            task.description?.let {
                tvTaskDescription.text = it
                tvTaskDescription.visibility = View.VISIBLE
            } ?: run {
                tvTaskDescription.visibility = View.GONE
            }

            // Format schedule info
            val scheduleText = buildString {
                append(DateTimeUtils.formatTime(task.startTime))
                if (task.recurrencePattern != Constants.RecurrencePattern.NONE) {
                    append(" • ")
                    append(Constants.RecurrencePattern.getDisplayName(task.recurrencePattern))
                }
            }
            tvTaskSchedule.text = scheduleText

            // Format reminder info
            val reminderText = formatReminderTime(task.reminderMinutesBefore)
            tvTaskReminder.text = "Reminder: $reminderText before"
        }
    }

    private fun formatReminderTime(minutes: Int): String {
        return when {
            minutes < 60 -> "$minutes minutes"
            minutes == 60 -> "1 hour"
            else -> "${minutes / 60} hours"
        }
    }

    private fun showMarkCompleteDialog() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_mark_complete, null)
        val notesInput = dialogView.findViewById<android.widget.EditText>(R.id.etCompletionNotes)

        AlertDialog.Builder(requireContext())
            .setTitle("Mark Task as Complete")
            .setView(dialogView)
            .setPositiveButton("Complete") { _, _ ->
                val notes = notesInput.text.toString().trim().takeIf { it.isNotEmpty() }
                scheduleViewModel.markTaskCompleted(
                    taskId!!,
                    currentUser.uid,
                    notes,
                    System.currentTimeMillis()
                )
                Toast.makeText(requireContext(), "Task marked as complete", Toast.LENGTH_SHORT).show()
                // Reload completion history
                scheduleViewModel.loadCompletedTasksForTask(taskId!!)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                taskId?.let {
                    scheduleViewModel.deleteTaskById(it)
                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scheduleViewModel.clearSelectedTask()
        _binding = null
    }
}

