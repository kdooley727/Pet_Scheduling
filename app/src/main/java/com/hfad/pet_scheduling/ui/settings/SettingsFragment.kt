package com.hfad.pet_scheduling.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.R
import com.hfad.pet_scheduling.databinding.FragmentSettingsBinding
import com.hfad.pet_scheduling.utils.CloudSyncManager
import com.hfad.pet_scheduling.utils.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private var cloudSyncManager: CloudSyncManager? = null

    companion object {
        private const val PREFS_THEME = "theme_preference"
        private const val PREFS_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val PREFS_DEFAULT_REMINDER = "default_reminder_minutes"
        private const val PREFS_LAST_SYNC = "last_sync_time"

        private const val THEME_LIGHT = "light"
        private const val THEME_DARK = "dark"
        private const val THEME_AUTO = "auto"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            sharedPreferences = requireContext().getSharedPreferences("app_preferences", android.content.Context.MODE_PRIVATE)
            val application = requireActivity().application as? PetSchedulingApplication
            cloudSyncManager = application?.cloudSyncManager

            setupToolbar()
            setupThemeToggle()
            setupNotifications()
            setupSync()
            setupAccount()
            setupAbout()
            loadSettings()
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "Error in onViewCreated", e)
            Toast.makeText(requireContext(), "Error loading settings: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupThemeToggle() {
        try {
            // Set initial selection based on saved preference
            val currentTheme = sharedPreferences.getString(PREFS_THEME, THEME_AUTO) ?: THEME_AUTO
            val initialButtonId = when (currentTheme) {
                THEME_LIGHT -> R.id.btnThemeLight
                THEME_DARK -> R.id.btnThemeDark
                THEME_AUTO -> R.id.btnThemeAuto
                else -> R.id.btnThemeAuto
            }

            binding.themeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    when (checkedId) {
                        R.id.btnThemeLight -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            sharedPreferences.edit().putString(PREFS_THEME, THEME_LIGHT).apply()
                        }
                        R.id.btnThemeDark -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            sharedPreferences.edit().putString(PREFS_THEME, THEME_DARK).apply()
                        }
                        R.id.btnThemeAuto -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            sharedPreferences.edit().putString(PREFS_THEME, THEME_AUTO).apply()
                        }
                    }
                }
            }
            
            // Set initial selection after listener is set up
            binding.themeToggleGroup.post {
                try {
                    if (binding.themeToggleGroup.checkedButtonId == -1) {
                        binding.themeToggleGroup.check(initialButtonId)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SettingsFragment", "Error checking theme button", e)
                    // Fallback: use findViewById to access buttons
                    try {
                        val button = binding.root.findViewById<com.google.android.material.button.MaterialButton>(initialButtonId)
                        button?.isChecked = true
                    } catch (e2: Exception) {
                        android.util.Log.e("SettingsFragment", "Error setting button checked state", e2)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "Error setting up theme toggle", e)
        }
    }

    private fun setupNotifications() {
        val notificationsEnabled = sharedPreferences.getBoolean(PREFS_NOTIFICATIONS_ENABLED, true)
        binding.switchNotifications.isChecked = notificationsEnabled

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(PREFS_NOTIFICATIONS_ENABLED, isChecked).apply()
            Toast.makeText(
                requireContext(),
                if (isChecked) "Notifications enabled" else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Setup default reminder dropdown
        try {
            val reminderOptions = Constants.ReminderTimes.ALL_TIMES.map { minutes ->
                formatReminderTime(minutes)
            }
            val reminderAdapter = android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                reminderOptions
            )
            binding.etDefaultReminder.setAdapter(reminderAdapter)

            val defaultReminder = sharedPreferences.getInt(PREFS_DEFAULT_REMINDER, 15)
            val defaultReminderText = formatReminderTime(defaultReminder)
            // Find the index of the default reminder in the list
            val defaultIndex = Constants.ReminderTimes.ALL_TIMES.indexOf(defaultReminder)
            if (defaultIndex >= 0 && defaultIndex < reminderOptions.size) {
                binding.etDefaultReminder.setText(reminderOptions[defaultIndex], false)
            } else {
                binding.etDefaultReminder.setText(defaultReminderText, false)
            }

            binding.etDefaultReminder.setOnItemClickListener { _, _, position, _ ->
                if (position < Constants.ReminderTimes.ALL_TIMES.size) {
                    val selectedMinutes = Constants.ReminderTimes.ALL_TIMES[position]
                    sharedPreferences.edit().putInt(PREFS_DEFAULT_REMINDER, selectedMinutes).apply()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "Error setting up notifications", e)
        }
    }

    private fun setupSync() {
        binding.btnSyncNow.setOnClickListener {
            cloudSyncManager?.fullSync() ?: run {
                Toast.makeText(requireContext(), "Sync not available", Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(requireContext(), "Syncing...", Toast.LENGTH_SHORT).show()
            
            // Update last sync time
            val currentTime = System.currentTimeMillis()
            sharedPreferences.edit().putLong(PREFS_LAST_SYNC, currentTime).apply()
            updateLastSyncTime()
        }

        // Observe sync status
        cloudSyncManager?.let { manager ->
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    manager.syncStatus.collect { status ->
                        when (status) {
                            CloudSyncManager.SyncStatus.SUCCESS -> {
                                val currentTime = System.currentTimeMillis()
                                sharedPreferences.edit().putLong(PREFS_LAST_SYNC, currentTime).apply()
                                updateLastSyncTime()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        updateLastSyncTime()
    }

    private fun updateLastSyncTime() {
        val lastSyncTime = sharedPreferences.getLong(PREFS_LAST_SYNC, 0L)
        if (lastSyncTime > 0) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            binding.tvLastSync.text = "Last synced: ${dateFormat.format(Date(lastSyncTime))}"
        } else {
            binding.tvLastSync.text = "Last synced: Never"
        }
    }

    private fun setupAccount() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            binding.tvUserEmail.text = currentUser.email ?: "No email"
        } else {
            binding.tvUserEmail.text = "Not signed in"
        }

        binding.btnManageSharedAccess.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_manageSharedAccessFragment)
        }

        binding.btnSignOut.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    // Clear database instance to prevent integrity issues
                    com.hfad.pet_scheduling.data.local.AppDatabase.clearInstance()
                    Toast.makeText(requireContext(), "Signed out", Toast.LENGTH_SHORT).show()
                    // Navigate to login
                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnDeleteAccount.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account")
                .setMessage("This will permanently delete your account and all your data. This action cannot be undone.")
                .setPositiveButton("Delete", null)
                .setNegativeButton("Cancel", null)
                .create()
            
            dialog.setOnShowListener {
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    deleteAccount()
                    dialog.dismiss()
                }
            }
            
            dialog.show()
        }
    }

    private fun deleteAccount() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "No user signed in", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Implement account deletion
        // This would require:
        // 1. Delete all user data from Firestore
        // 2. Delete user from Firebase Auth
        // 3. Clear local database
        // 4. Navigate to login

        Toast.makeText(
            requireContext(),
            "Account deletion not yet implemented",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setupAbout() {
        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(
                requireContext().packageName,
                0
            )
            val versionName = packageInfo.versionName
            binding.tvAppVersion.text = "Version $versionName"
        } catch (e: Exception) {
            binding.tvAppVersion.text = "Version 1.0.0"
        }
    }

    private fun loadSettings() {
        // Settings are loaded in their respective setup methods
        // This method can be used for any additional initialization
    }

    private fun formatReminderTime(minutes: Int): String {
        return when (minutes) {
            0 -> "None"
            1 -> "1 minute"
            else -> "$minutes minutes"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

