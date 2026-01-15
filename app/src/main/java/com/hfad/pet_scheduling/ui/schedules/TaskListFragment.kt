package com.hfad.pet_scheduling.ui.schedules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.R
import com.hfad.pet_scheduling.databinding.FragmentTaskListBinding
import com.hfad.pet_scheduling.viewmodels.ScheduleViewModel
import com.hfad.pet_scheduling.viewmodels.ViewModelFactory
import com.hfad.pet_scheduling.viewmodels.PetViewModel
import com.hfad.pet_scheduling.utils.GeminiHelper
import com.hfad.pet_scheduling.utils.FreeAIHelper
import com.hfad.pet_scheduling.data.entities.ScheduleTask
import com.hfad.pet_scheduling.utils.ExportHelper
import com.hfad.pet_scheduling.utils.Constants
import com.hfad.pet_scheduling.utils.DateTimeUtils
import com.hfad.pet_scheduling.utils.QuotaExceededException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class TaskListFragment : Fragment() {
    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var petViewModel: PetViewModel
    private lateinit var taskAdapter: TaskAdapter
    private var petId: String? = null
    private var allTasks: List<ScheduleTask> = emptyList()
    private var currentFilter: String = "all" // "all", "today", "week"
    private var selectedCategory: String? = null
    private var searchQuery: String = ""
    private var completedTaskIdsToday: Set<String> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get petId from arguments
        petId = arguments?.getString("petId")
        if (petId == null) {
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
        petViewModel = ViewModelProvider(this, factory)[PetViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupFilters()
        setupSearch()

        // Load tasks for this pet
        scheduleViewModel.loadTasksForPet(petId!!)
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                // Navigate to task detail screen
                val bundle = Bundle().apply {
                    putString("taskId", task.taskId)
                }
                findNavController().navigate(R.id.action_taskListFragment_to_taskDetailFragment, bundle)
            },
            onMarkComplete = { task ->
                markTaskComplete(task)
            }
        )

        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.tasks.collect { tasks ->
                    allTasks = tasks
                    applyFilters()

                    val taskIds = tasks.map { it.taskId }
                    if (taskIds.isNotEmpty()) {
                        val now = System.currentTimeMillis()
                        scheduleViewModel.loadCompletedTasksInDateRange(
                            taskIds,
                            DateTimeUtils.getStartOfDay(now),
                            DateTimeUtils.getEndOfDay(now)
                        )
                    } else {
                        completedTaskIdsToday = emptySet()
                        taskAdapter.setCompletedTaskIds(completedTaskIdsToday)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.isLoading.collect { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.completedTasks.collect { completed ->
                    completedTaskIdsToday = completed.map { it.taskId }.toSet()
                    taskAdapter.setCompletedTaskIds(completedTaskIdsToday)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                scheduleViewModel.errorMessage.collect { error ->
                    error?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        scheduleViewModel.clearError()
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.fabAddTask.setOnClickListener {
            val bundle = Bundle().apply {
                putString("petId", petId)
            }
            findNavController().navigate(R.id.action_taskListFragment_to_addEditTaskFragment, bundle)
        }

        binding.fabAISuggestions.setOnClickListener {
            showTemplateScheduleDialog()
        }

        binding.fabExport.setOnClickListener {
            showExportDialog()
        }
    }

    private fun showExportDialog() {
        val options = arrayOf("Export as HTML (for PDF)", "Export as CSV")
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Export Pet Schedule")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportToHTML()
                    1 -> exportToCSV()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTemplateScheduleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create template schedule?")
            .setMessage(
                "This will generate a starter schedule for this pet. " +
                    "You can edit or delete tasks afterward."
            )
            .setPositiveButton("Create") { _, _ ->
                generateAISuggestions()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportToHTML() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        petId?.let { id ->
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    binding.progressBar.visibility = View.VISIBLE
                    val application = requireActivity().application as PetSchedulingApplication
                    
                    // Get pet data
                    val pet = withContext(Dispatchers.IO) {
                        application.petRepository.getPetByIdSuspend(id)
                    }
                    
                    if (pet == null) {
                        Toast.makeText(requireContext(), "Pet not found", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                        return@launch
                    }

                    // Get all tasks (including inactive) for completed task details
                    val allTasksFlow = application.scheduleRepository.getActiveTasksByPet(id)
                    val activeTasks = withContext(Dispatchers.IO) {
                        allTasksFlow.first()
                    }

                    // Get completed tasks
                    val completedTasks = withContext(Dispatchers.IO) {
                        application.scheduleRepository.getCompletedTasksByPet(id)
                    }
                    
                    // Get task details for completed tasks (including inactive ones)
                    val allTaskIds = (activeTasks.map { it.taskId } + completedTasks.map { it.taskId }).distinct()
                    val allTasksMap = mutableMapOf<String, ScheduleTask>()
                    activeTasks.forEach { allTasksMap[it.taskId] = it }
                    // Fetch any missing tasks
                    allTaskIds.forEach { taskId ->
                        if (!allTasksMap.containsKey(taskId)) {
                            application.scheduleRepository.getTaskByIdSuspend(taskId)?.let {
                                allTasksMap[taskId] = it
                            }
                        }
                    }
                    val allTasksForExport: List<ScheduleTask> = allTasksMap.values.toList()

                    // Export
                    val exportHelper = ExportHelper(requireContext())
                    val uri = exportHelper.exportToHTML(pet, allTasksForExport, completedTasks)
                    
                    if (uri != null) {
                        exportHelper.shareFile(uri, "Pet_Schedule_${pet.name}.html", "text/html")
                        Toast.makeText(requireContext(), "Export ready to share", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Error creating export", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("TaskListFragment", "Error exporting", e)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun exportToCSV() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        petId?.let { id ->
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    binding.progressBar.visibility = View.VISIBLE
                    val application = requireActivity().application as PetSchedulingApplication
                    
                    // Get pet data
                    val pet = withContext(Dispatchers.IO) {
                        application.petRepository.getPetByIdSuspend(id)
                    }
                    
                    if (pet == null) {
                        Toast.makeText(requireContext(), "Pet not found", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                        return@launch
                    }

                    // Get all tasks (including inactive) for completed task details
                    val allTasksFlow = application.scheduleRepository.getActiveTasksByPet(id)
                    val activeTasks = withContext(Dispatchers.IO) {
                        allTasksFlow.first()
                    }

                    // Get completed tasks
                    val completedTasks = withContext(Dispatchers.IO) {
                        application.scheduleRepository.getCompletedTasksByPet(id)
                    }
                    
                    // Get task details for completed tasks (including inactive ones)
                    val allTaskIds = (activeTasks.map { it.taskId } + completedTasks.map { it.taskId }).distinct()
                    val allTasksMap = mutableMapOf<String, ScheduleTask>()
                    activeTasks.forEach { allTasksMap[it.taskId] = it }
                    // Fetch any missing tasks
                    allTaskIds.forEach { taskId ->
                        if (!allTasksMap.containsKey(taskId)) {
                            application.scheduleRepository.getTaskByIdSuspend(taskId)?.let {
                                allTasksMap[taskId] = it
                            }
                        }
                    }
                    val allTasksForExport = allTasksMap.values.toList()

                    // Export
                    val exportHelper = ExportHelper(requireContext())
                    val uri = exportHelper.exportToCSV(pet, allTasksForExport, completedTasks)
                    
                    if (uri != null) {
                        exportHelper.shareFile(uri, "Pet_Schedule_${pet.name}.csv", "text/csv")
                        Toast.makeText(requireContext(), "Export ready to share", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Error creating export", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("TaskListFragment", "Error exporting", e)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                searchQuery = binding.etSearch.text.toString().trim()
                applyFilters()
                // Hide keyboard
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                true
            } else {
                false
            }
        }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyFilters()
            }
        })

        // Clear search when clear icon is clicked
        binding.tilSearch.setEndIconOnClickListener {
            binding.etSearch.setText("")
            searchQuery = ""
            applyFilters()
        }
    }

    private fun setupFilters() {
        // Setup category filter
        val categoryAdapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            listOf("All Categories") + Constants.TaskCategory.ALL_CATEGORIES.map {
                Constants.TaskCategory.getDisplayName(it)
            }
        )
        binding.etCategoryFilter.setAdapter(categoryAdapter)
        binding.etCategoryFilter.setText("All Categories", false)
        binding.etCategoryFilter.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = if (position == 0) {
                null
            } else {
                Constants.TaskCategory.ALL_CATEGORIES[position - 1]
            }
            applyFilters()
        }

        // Setup date filter chips
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = "all"
                applyFilters()
            }
        }

        binding.chipToday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = "today"
                applyFilters()
            }
        }

        binding.chipThisWeek.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = "week"
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        var filteredTasks = allTasks

        // Apply search filter
        if (searchQuery.isNotEmpty()) {
            val query = searchQuery.lowercase()
            filteredTasks = filteredTasks.filter { task ->
                task.title.lowercase().contains(query) ||
                task.description?.lowercase()?.contains(query) == true ||
                task.category.lowercase().contains(query)
            }
        }

        // Apply category filter
        selectedCategory?.let { category ->
            filteredTasks = filteredTasks.filter { it.category == category }
        }

        // Apply date filter
        val now = System.currentTimeMillis()
        filteredTasks = when (currentFilter) {
            "today" -> {
                val startOfDay = DateTimeUtils.getStartOfDay(now)
                val endOfDay = DateTimeUtils.getEndOfDay(now)
                filteredTasks.filter { it.startTime in startOfDay..endOfDay }
            }
            "week" -> {
                val startOfWeek = DateTimeUtils.getStartOfWeek(now)
                val endOfWeek = DateTimeUtils.getEndOfWeek(now)
                filteredTasks.filter { it.startTime in startOfWeek..endOfWeek }
            }
            else -> filteredTasks
        }

        // Update UI
        if (filteredTasks.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerViewTasks.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerViewTasks.visibility = View.VISIBLE
            taskAdapter.submitList(filteredTasks)
        }
    }

    private fun markTaskComplete(task: ScheduleTask) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        scheduleViewModel.markTaskCompleted(
            task.taskId,
            currentUser.uid,
            null,
            System.currentTimeMillis()
        )
        Toast.makeText(requireContext(), "Task marked as complete", Toast.LENGTH_SHORT).show()
    }

    private fun generateAISuggestions() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Get pet information
        petId?.let { id ->
            CoroutineScope(Dispatchers.Main).launch {
                binding.progressBar.visibility = View.VISIBLE
                val application = requireActivity().application as PetSchedulingApplication
                
                // Get pet outside try block so it's accessible in catch blocks
                val pet = withContext(Dispatchers.IO) {
                    application.petRepository.getPetByIdSuspend(id)
                }

                if (pet == null) {
                    Toast.makeText(requireContext(), "Pet not found", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    return@launch
                }

                try {

                    // Calculate pet age if birth date is available
                    val petAge = pet.birthDate?.let {
                        val ageInDays = (System.currentTimeMillis() - it) / (1000 * 60 * 60 * 24)
                        when {
                            ageInDays < 30 -> "$ageInDays days"
                            ageInDays < 365 -> "${ageInDays / 30} months"
                            else -> "${ageInDays / 365} years"
                        }
                    }

                    // Try free AI first (no API key required)
                    var suggestions: String?
                    val freeAIHelper = FreeAIHelper()
                    
                    android.util.Log.d("TaskListFragment", "Trying free AI first...")
                    suggestions = withContext(Dispatchers.IO) {
                        try {
                            freeAIHelper.generatePetSchedule(
                                petName = pet.name,
                                petType = pet.type,
                                petBreed = pet.breed,
                                petAge = petAge
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("TaskListFragment", "Error with free AI", e)
                            null
                        }
                    }
                    
                    // If free AI failed, try Gemini as fallback (if API key is configured)
                    if (suggestions.isNullOrEmpty()) {
                        val apiKey = com.hfad.pet_scheduling.BuildConfig.GEMINI_API_KEY
                        if (apiKey.isNotEmpty()) {
                            android.util.Log.d("TaskListFragment", "Free AI failed, trying Gemini...")
                            val geminiHelper = GeminiHelper(apiKey)
                            suggestions = withContext(Dispatchers.IO) {
                                try {
                                    geminiHelper.generatePetSchedule(
                                        petName = pet.name,
                                        petType = pet.type,
                                        petBreed = pet.breed,
                                        petAge = petAge
                                    )
                                } catch (e: Exception) {
                                    android.util.Log.e("TaskListFragment", "Error generating suggestions with Gemini", e)
                                    null
                                }
                            }
                        }
                    }

                    if (suggestions != null && suggestions.isNotEmpty()) {
                        parseAndCreateTasks(suggestions, currentUser.uid)
                        Toast.makeText(requireContext(), "AI suggestions added! (Free AI)", Toast.LENGTH_SHORT).show()
                    } else {
                        android.util.Log.w("TaskListFragment", "Suggestions is null or empty")
                        Toast.makeText(
                            requireContext(), 
                            "Failed to generate suggestions. The AI service may be temporarily unavailable. Please try again in a few moments.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: QuotaExceededException) {
                    android.util.Log.w("TaskListFragment", "Quota exceeded, trying free AI fallback", e)
                    // Try free AI as fallback when Gemini quota is exceeded
                    try {
                        val freeAIHelper = FreeAIHelper()
                        val petAge = pet.birthDate?.let {
                            val ageInDays = (System.currentTimeMillis() - it) / (1000 * 60 * 60 * 24)
                            when {
                                ageInDays < 30 -> "$ageInDays days"
                                ageInDays < 365 -> "${ageInDays / 30} months"
                                else -> "${ageInDays / 365} years"
                            }
                        }
                        val suggestions = withContext(Dispatchers.IO) {
                            freeAIHelper.generatePetSchedule(
                                petName = pet.name,
                                petType = pet.type,
                                petBreed = pet.breed,
                                petAge = petAge
                            )
                        }
                        if (suggestions != null && suggestions.isNotEmpty()) {
                            parseAndCreateTasks(suggestions, currentUser.uid)
                            Toast.makeText(requireContext(), "AI suggestions added! (Using free AI)", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Gemini quota exceeded. Free AI is also unavailable. Please try again later.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e2: Exception) {
                        android.util.Log.e("TaskListFragment", "Free AI fallback also failed", e2)
                        Toast.makeText(
                            requireContext(),
                            "Both AI services are unavailable. Please try again later.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("TaskListFragment", "Error in generateAISuggestions", e)
                    Toast.makeText(
                        requireContext(), 
                        "Error generating suggestions. Please try again in a few moments.",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private suspend fun parseAndCreateTasks(suggestionsJson: String, userId: String) {
        try {
            // Clean the JSON string - remove markdown code blocks if present
            var cleanedJson = suggestionsJson.trim()
            if (cleanedJson.startsWith("```json")) {
                cleanedJson = cleanedJson.removePrefix("```json").trim()
            }
            if (cleanedJson.startsWith("```")) {
                cleanedJson = cleanedJson.removePrefix("```").trim()
            }
            if (cleanedJson.endsWith("```")) {
                cleanedJson = cleanedJson.removeSuffix("```").trim()
            }
            
            android.util.Log.d("TaskListFragment", "Cleaned JSON: $cleanedJson")
            
            // Try to parse JSON array
            val jsonArray = JSONArray(cleanedJson)
            val tasksToCreate = mutableListOf<ScheduleTask>()

            for (i in 0 until jsonArray.length()) {
                val taskJson = jsonArray.getJSONObject(i)
                val title = taskJson.getString("title")
                val description = taskJson.optString("description").takeIf { it.isNotEmpty() }
                val category = taskJson.optString("category", Constants.TaskCategory.OTHER)
                val suggestedTime = taskJson.optString("suggestedTime", "08:00")
                val recurrencePattern = taskJson.optString("recurrencePattern", Constants.RecurrencePattern.DAILY)

                // Parse time (HH:mm format)
                val (hour, minute) = suggestedTime.split(":").map { it.toInt() }
                val calendar = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, hour)
                    set(java.util.Calendar.MINUTE, minute)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }

                val task = ScheduleTask(
                    petId = petId!!,
                    title = title,
                    description = description,
                    category = category,
                    startTime = calendar.timeInMillis,
                    recurrencePattern = recurrencePattern,
                    reminderMinutesBefore = Constants.ReminderTimes.MINUTES_15,
                    createdByUserId = userId
                )
                tasksToCreate.add(task)
            }

            // Save all tasks
            withContext(Dispatchers.IO) {
                tasksToCreate.forEach { task ->
                    scheduleViewModel.saveTask(task)
                }
            }
        } catch (e: Exception) {
            // If JSON parsing fails, show the raw text as a toast
            android.util.Log.e("TaskListFragment", "Failed to parse AI suggestions", e)
            Toast.makeText(requireContext(), "Could not parse suggestions. Please add tasks manually.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

