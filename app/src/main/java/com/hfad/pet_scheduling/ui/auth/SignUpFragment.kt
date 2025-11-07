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
import com.hfad.pet_scheduling.databinding.FragmentSignUpBinding
import com.hfad.pet_scheduling.utils.GoogleSignInHelper
import com.hfad.pet_scheduling.viewmodels.AuthViewModel
import com.hfad.pet_scheduling.viewmodels.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
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
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
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
        val webClientId = getString(R.string.default_web_client_id)
        googleSignInHelper = GoogleSignInHelper(requireActivity())
        googleSignInHelper.initialize(webClientId)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        authViewModel.isAuthenticated.observe(viewLifecycleOwner) { isAuthenticated ->
            if (isAuthenticated) {
                findNavController().navigate(R.id.action_signUpFragment_to_petListFragment)
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
            binding.btnSignUp.isEnabled = !isLoading
        }
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val displayName = binding.etDisplayName.text.toString().trim()

            if (validateInput(email, password, confirmPassword)) {
                authViewModel.signUp(email, password, displayName)
            }
        }

        binding.btnBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInHelper.getSignInIntent()
            signInIntent?.let { googleSignInLauncher.launch(it) }
                ?: Toast.makeText(
                    requireContext(),
                    "Google Sign-In not configured. Please check your web client ID.",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun handleGoogleSignIn(account: GoogleSignInAccount) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val result = com.google.firebase.auth.FirebaseAuth.getInstance()
                    .signInWithCredential(credential).await()
                
                // Navigation will happen automatically via auth state observer
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Google sign in failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun validateInput(
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
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
        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

