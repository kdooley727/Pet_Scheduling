package com.hfad.pet_scheduling

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        return navHostFragment?.navController?.navigateUp() ?: super.onSupportNavigateUp()
    }
}
