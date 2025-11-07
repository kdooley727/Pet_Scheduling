package com.hfad.pet_scheduling.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.GoogleAuthProvider
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.R
import com.hfad.pet_scheduling.databinding.FragmentLoginBinding
import com.hfad.pet_scheduling.utils.GoogleSignInHelper
import com.hfad.pet_scheduling.viewmodels.AuthViewModel
import com.hfad.pet_scheduling.viewmodels.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel
    private lateinit var googleSignInHelper: GoogleSignInHelper

    // Google Sign-In result launcher
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val account = googleSignInHelper.handleSignInResult(result.data)
        account?.let { handleGoogleSignIn(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val application = requireActivity().application as PetSchedulingApplication
        val factory = ViewModelFactory(
            application.petRepository,
            application.scheduleRepository
        )
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        // Initialize Google Sign-In
        // TODO: Replace with your actual web client ID from Firebase Console
        // Get it from: Firebase Console > Authentication > Sign-in method > Google > Web SDK configuration
        val webClientId = getString(R.string.default_web_client_id) // You'll need to add this to strings.xml
        googleSignInHelper = GoogleSignInHelper(requireActivity())
        googleSignInHelper.initialize(webClientId)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        authViewModel.isAuthenticated.observe(viewLifecycleOwner) { isAuthenticated ->
            if (isAuthenticated) {
                findNavController().navigate(R.id.action_loginFragment_to_petListFragment)
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            binding.btnSignUp.isEnabled = !isLoading
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (validateInput(email, password)) {
                authViewModel.signIn(email, password)
            }
        }

        binding.btnSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.btnForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                authViewModel.sendPasswordResetEmail(email)
                Toast.makeText(
                    requireContext(),
                    "Password reset email sent",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter your email first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInHelper.getSignInIntent()
            if (signInIntent != null) {
                googleSignInLauncher.launch(signInIntent)
            } else {
                android.util.Log.e("LoginFragment", "Google Sign-In intent is null. Check web client ID.")
                Toast.makeText(
                    requireContext(),
                    "Google Sign-In not configured. Please check your web client ID.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun handleGoogleSignIn(account: GoogleSignInAccount) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val idToken = account.idToken
                if (idToken == null) {
                    Toast.makeText(
                        requireContext(),
                        "Google sign in failed: No ID token received",
                        Toast.LENGTH_LONG
                    ).show()
                    android.util.Log.e("LoginFragment", "Google Sign-In: ID token is null")
                    return@launch
                }
                
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = com.google.firebase.auth.FirebaseAuth.getInstance()
                    .signInWithCredential(credential).await()
                
                android.util.Log.d("LoginFragment", "Google Sign-In successful: ${result.user?.email}")
                // Navigation will happen automatically via auth state observer
            } catch (e: Exception) {
                android.util.Log.e("LoginFragment", "Google sign in failed", e)
                val errorMessage = when {
                    e.message?.contains("network") == true -> "Network error. Please check your internet connection."
                    e.message?.contains("10") == true -> "Google Sign-In configuration error. Check SHA-1 fingerprint in Firebase."
                    else -> "Google sign in failed: ${e.message}"
                }
                Toast.makeText(
                    requireContext(),
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email format"
            return false
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return false
        }
        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

