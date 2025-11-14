package com.hfad.pet_scheduling

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.hfad.pet_scheduling.utils.NotificationRescheduler

class MainActivity : AppCompatActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "✅ Notification permission granted")
        } else {
            android.util.Log.w("MainActivity", "❌ Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply theme preference before setting content view
        applyThemePreference()
        
        setContentView(R.layout.activity_main)
        
        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    android.util.Log.d("MainActivity", "✅ Notification permission already granted")
                }
                else -> {
                    android.util.Log.d("MainActivity", "📢 Requesting notification permission")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        // Setup navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController

        navController?.let {
            // Note: setupActionBarWithNavController is not used since we're using NoActionBar theme
            // If you add a Toolbar later, you can set it up with:
            // setSupportActionBar(toolbar)
            // setupActionBarWithNavController(it)

            // Check if user is already authenticated
            // Use post to ensure navigation happens after fragment is ready
            navHostFragment?.view?.post {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    // User is logged in, navigate to pet list
                    try {
                        it.navigate(R.id.petListFragment)
                    } catch (e: Exception) {
                        // Navigation might already be at destination, ignore
                        e.printStackTrace()
                    }
                    
                    // Reschedule notifications for all active tasks
                    // This ensures notifications persist after device restarts
                    android.util.Log.d("MainActivity", "🔄 Rescheduling notifications on app startup")
                    val notificationRescheduler = NotificationRescheduler(this)
                    notificationRescheduler.rescheduleAllNotifications()
                    
                    // Sync data with Firebase Firestore
                    val application = applicationContext as PetSchedulingApplication
                    android.util.Log.d("MainActivity", "☁️ Starting cloud sync on app startup")
                    application.cloudSyncManager.fullSync()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        return navHostFragment?.navController?.navigateUp() ?: super.onSupportNavigateUp()
    }

    private fun applyThemePreference() {
        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val themePreference = sharedPreferences.getString("theme_preference", "auto") ?: "auto"
        
        when (themePreference) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "auto" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
