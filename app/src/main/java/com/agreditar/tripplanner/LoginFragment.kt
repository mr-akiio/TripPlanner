package com.agreditar.tripplanner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    // Firebase Authentication instance
    private lateinit var auth: FirebaseAuth

    // UI elements
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var errorLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Initialize Firebase Authentication
        auth = Firebase.auth

        // Get references to UI elements
        emailEditText = view.findViewById(R.id.email_login)
        passwordEditText = view.findViewById(R.id.password_login)
        loginButton = view.findViewById(R.id.loginButton)
        registerLink = view.findViewById(R.id.registerLink)
        errorLayout = view.findViewById(R.id.login_error)

        // Set click listener for the login button
        loginButton.setOnClickListener {
            loginUser()
        }

        // Set click listener for the register link
        registerLink.setOnClickListener {
            // Navigate to the registration fragment
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }

    // Function to handle user login
    private fun loginUser() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Reset error messages
        errorLayout.error = null
        var hasErrors = false

        // Basic validation examples (you should add more complex checks)
        if (email.isEmpty()) {
            errorLayout.error = "Email is required"
            hasErrors = true
        }
        if (password.isEmpty()) {
            errorLayout.error = "Password is required"
            hasErrors = true
        }
        // Proceed if there are no errors
        if (!hasErrors) {
            // Firebase sign-in with email and password
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign-in success
                        Log.d("LoginFragment", "signInWithEmail:success")
                        val user = auth.currentUser
                        Toast.makeText(
                            requireContext(),
                            "Login success.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        // Navigate to next fragment, or any other action needed.
                        // For example, to navigate to the main fragment:
                        // findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                        //Or:
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)

                    } else {
                        // Sign-in failed
                        Log.w("LoginFragment", "signInWithEmail:failure", task.exception)
                        errorLayout.error = "Invalid credentials."
                    }
                }
        }
    }
}