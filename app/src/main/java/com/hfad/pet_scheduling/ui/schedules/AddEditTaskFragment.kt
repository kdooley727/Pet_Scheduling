package com.hfad.pet_scheduling.ui.schedules

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.R
import com.hfad.pet_scheduling.data.local.entities.ScheduleTask
import com.hfad.pet_scheduling.databinding.FragmentAddEditTaskBinding
import com.hfad.pet_scheduling.data.TaskTemplate
import com.hfad.pet_scheduling.utils.Constants
import com.hfad.pet_scheduling.utils.DateTimeUtils
import com.hfad.pet_scheduling.viewmodels.ScheduleViewModel
import com.hfad.pet_scheduling.viewmodels.ViewModelFactory
import java.util.*

class AddEditTaskFragment : Fragment() {
    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var scheduleViewModel: ScheduleViewModel
    private var petId: String? = null
    private var taskId: String? = null
    private var isEditMode = false

    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedTime: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get arguments
        petId = arguments?.getString("petId")
        taskId = arguments?.getString("taskId")
        isEditMode = taskId != null

        if (petId == null && !isEditMode) {
            Toast.makeText(requireContext(), "Pet ID is required", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        val application = requireActivity().application as PetSchedulingApplication
        val factory = ViewModelFactory(
            application.petRepository,
            application.scheduleRepository,
            application
        )
        scheduleViewModel = ViewModelProvider(this, factory)[ScheduleViewModel::class.java]

        setupUI()
        setupObservers()
        setupClickListeners()

        if (isEditMode) {
            taskId?.let { scheduleViewModel.getTaskById(it) }
            // Hide template button in edit mode
            binding.btnUseTemplate.visibility = View.GONE
        } else {
            // Show template button for new tasks
            binding.btnUseTemplate.visibility = View.VISIBLE
            // Set default values for new task
            selectedDate = Calendar.getInstance()
            selectedTime = Calendar.getInstance()
            updateDateDisplay()
            updateTimeDisplay()
            // Set default reminder to 15 minutes
            binding.etReminderMinutes.setText(formatReminderTime(15), false)
        }
    }

    private fun setupUI() {
        binding.toolbar.title = if (isEditMode) "Edit Task" else "Add Task"

        // Setup category dropdown
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            Constants.TaskCategory.ALL_CATEGORIES.map { Constants.TaskCategory.getDisplayName(it) }
        )
        binding.etTaskCategory.setAdapter(categoryAdapter)

        // Setup recurrence pattern dropdown
        val recurrenceAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            Constants.RecurrencePattern.ALL_PATTERNS.map { Constants.RecurrencePattern.getDisplayName(it) }
        )
        binding.etRecurrencePattern.setAdapter(recurrenceAdapter)

        // Setup reminder minutes dropdown
        val reminderAdapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            Constants.ReminderTimes.ALL_TIMES.map { formatReminderTime(it) }
        )
        binding.etReminderMinutes.setAdapter(reminderAdapter)
    }

    private fun setupObservers() {
        scheduleViewModel.selectedTask.observe(viewLifecycleOwner) { task ->
            task?.let {
                populateForm(it)
                petId = it.petId
            }
        }

        scheduleViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveTask.isEnabled = !isLoading
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

        binding.btnUseTemplate.setOnClickListener {
            showTemplateDialog()
        }

        binding.etTaskDate.setOnClickListener {
            showDatePicker()
        }

        binding.etTaskTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnSaveTask.setOnClickListener {
            saveTask()
        }
    }

    private fun populateForm(task: ScheduleTask) {
        binding.etTaskTitle.setText(task.title)
        binding.etTaskDescription.setText(task.description ?: "")
        
        val categoryDisplay = Constants.TaskCategory.getDisplayName(task.category)
        binding.etTaskCategory.setText(categoryDisplay, false)

        selectedDate.timeInMillis = task.startTime
        selectedTime.timeInMillis = task.startTime
        updateDateDisplay()
        updateTimeDisplay()

        val recurrenceDisplay = Constants.RecurrencePattern.getDisplayName(task.recurrencePattern)
        binding.etRecurrencePattern.setText(recurrenceDisplay, false)

        val reminderDisplay = formatReminderTime(task.reminderMinutesBefore)
        binding.etReminderMinutes.setText(reminderDisplay, false)
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                updateDateDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
                updateTimeDisplay()
            },
            selectedTime.get(Calendar.HOUR_OF_DAY),
            selectedTime.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun updateDateDisplay() {
        binding.etTaskDate.setText(DateTimeUtils.formatDate(selectedDate.timeInMillis))
    }

    private fun updateTimeDisplay() {
        binding.etTaskTime.setText(DateTimeUtils.formatTime(selectedTime.timeInMillis))
    }

    private fun formatReminderTime(minutes: Int): String {
        return when {
            minutes < 60 -> "$minutes minutes"
            minutes == 60 -> "1 hour"
            else -> "${minutes / 60} hours"
        }
    }

    private fun saveTask() {
        val title = binding.etTaskTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.tilTaskTitle.error = "Title is required"
            return
        }
        binding.tilTaskTitle.error = null

        val categoryText = binding.etTaskCategory.text.toString().trim()
        val category = Constants.TaskCategory.ALL_CATEGORIES.find {
            Constants.TaskCategory.getDisplayName(it) == categoryText
        } ?: Constants.TaskCategory.OTHER

        val recurrenceText = binding.etRecurrencePattern.text.toString().trim()
        val recurrencePattern = Constants.RecurrencePattern.ALL_PATTERNS.find {
            Constants.RecurrencePattern.getDisplayName(it) == recurrenceText
        } ?: Constants.RecurrencePattern.NONE

        val reminderText = binding.etReminderMinutes.text.toString().trim()
        val reminderMinutes = if (reminderText.isEmpty()) {
            // Default to 15 minutes if not set
            android.util.Log.d("AddEditTaskFragment", "⚠️ No reminder selected, defaulting to 15 minutes")
            15
        } else {
            parseReminderTime(reminderText)
        }
        
        android.util.Log.d("AddEditTaskFragment", "📝 Reminder text: '$reminderText', parsed to: $reminderMinutes minutes")

        // Combine date and time
        val combinedDateTime = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
            set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val taskTime = combinedDateTime.timeInMillis
        val currentTime = System.currentTimeMillis()
        val reminderTime = taskTime - (reminderMinutes * 60 * 1000L)
        
        // Validate that reminder time is in the future
        if (reminderTime <= currentTime) {
            val reminderTimeStr = DateTimeUtils.formatDateTime(reminderTime)
            val currentTimeStr = DateTimeUtils.formatDateTime(currentTime)
            android.util.Log.w("AddEditTaskFragment", "⚠️ Reminder time ($reminderTimeStr) is in the past (current: $currentTimeStr)")
            
            Toast.makeText(
                requireContext(),
                "Warning: Reminder time is in the past. Task will be saved but no notification will be scheduled. Please set a future time.",
                Toast.LENGTH_LONG
            ).show()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val task = if (isEditMode && taskId != null) {
            scheduleViewModel.selectedTask.value?.copy(
                title = title,
                description = binding.etTaskDescription.text.toString().trim().takeIf { it.isNotEmpty() },
                category = category,
                startTime = combinedDateTime.timeInMillis,
                recurrencePattern = recurrencePattern,
                reminderMinutesBefore = reminderMinutes
            ) ?: return
        } else {
            ScheduleTask(
                petId = petId!!,
                title = title,
                description = binding.etTaskDescription.text.toString().trim().takeIf { it.isNotEmpty() },
                category = category,
                startTime = combinedDateTime.timeInMillis,
                recurrencePattern = recurrencePattern,
                reminderMinutesBefore = reminderMinutes,
                createdByUserId = currentUser.uid
            )
        }

        android.util.Log.d("AddEditTaskFragment", "💾 Saving task: title='${task.title}', startTime=${DateTimeUtils.formatDateTime(task.startTime)}, reminder=${task.reminderMinutesBefore} min")
        
        if (isEditMode) {
            android.util.Log.d("AddEditTaskFragment", "✏️ Updating existing task")
            scheduleViewModel.updateTask(task)
            Toast.makeText(requireContext(), "Task updated successfully", Toast.LENGTH_SHORT).show()
        } else {
            android.util.Log.d("AddEditTaskFragment", "➕ Creating new task")
            scheduleViewModel.saveTask(task)
            Toast.makeText(requireContext(), "Task saved successfully", Toast.LENGTH_SHORT).show()
        }
        
        // Navigate back after a short delay to allow save to complete
        requireView().postDelayed({
            findNavController().popBackStack()
        }, 300)
    }

    private fun parseReminderTime(text: String): Int {
        return when {
            text.contains("hour") -> {
                val hours = text.replace("hours", "").replace("hour", "").trim().toIntOrNull() ?: 1
                hours * 60
            }
            text.contains("minute") -> {
                text.replace("minutes", "").replace("minute", "").trim().toIntOrNull() ?: 15
            }
            else -> {
                text.toIntOrNull() ?: 15
            }
        }
    }

    private fun showTemplateDialog() {
        val templates = TaskTemplate.getDefaultTemplates()
        
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select a Template")
            .setItems(
                templates.map { "${Constants.TaskCategory.getDisplayName(it.category)}: ${it.name}" }.toTypedArray()
            ) { _, which ->
                val selectedTemplate = templates[which]
                applyTemplate(selectedTemplate)
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }

    private fun applyTemplate(template: TaskTemplate) {
        // Fill form with template data
        binding.etTaskTitle.setText(template.name)
        binding.etTaskDescription.setText(template.description ?: "")
        
        // Set category
        val categoryDisplayName = Constants.TaskCategory.getDisplayName(template.category)
        binding.etTaskCategory.setText(categoryDisplayName, false)
        
        // Set recurrence pattern
        val recurrenceDisplayName = Constants.RecurrencePattern.getDisplayName(template.recurrencePattern)
        binding.etRecurrencePattern.setText(recurrenceDisplayName, false)
        
        // Set reminder time
        binding.etReminderMinutes.setText(formatReminderTime(template.reminderMinutesBefore), false)
        
        // Parse and set time from template
        val (hour, minute) = template.defaultTime.split(":").map { it.toInt() }
        selectedTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        updateTimeDisplay()
        
        Toast.makeText(requireContext(), "Template applied: ${template.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scheduleViewModel.clearSelectedTask()
        _binding = null
    }
}

