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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView
    private lateinit var errorLayout: TextInputLayout
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        auth = Firebase.auth
        db = Firebase.firestore
        usernameEditText = view.findViewById(R.id.username_register)
        passwordEditText = view.findViewById(R.id.password_register)
        emailEditText = view.findViewById(R.id.email_register)
        confirmPasswordEditText = view.findViewById(R.id.confirm_password_register)
        registerButton = view.findViewById(R.id.registerButton)
        loginLink = view.findViewById(R.id.loginLink)
        errorLayout = view.findViewById(R.id.register_error)

        registerButton.setOnClickListener {
            registerUser()
        }

        loginLink.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
        }

        return view
    }

    private fun registerUser() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()
        val email = emailEditText.text.toString()

        // Reset error messages
        errorLayout.error = null
        var hasErrors = false

        // Basic validation examples (you should add more complex checks)
        if (username.isEmpty()) {
            errorLayout.error = "Username is required"
            hasErrors = true
        }
        if (email.isEmpty()) {
            errorLayout.error = "Email is required"
            hasErrors = true
        }
        if (password.isEmpty()) {
            errorLayout.error = "Password is required"
            hasErrors = true
        }
        if (confirmPassword.isEmpty()) {
            errorLayout.error = "Confirm Password is required"
            hasErrors = true
        }
        if (password != confirmPassword) {
            errorLayout.error = "Passwords do not match"
            hasErrors = true
        }

        if (!hasErrors) {
            // Check if the username already exists in Firestore
            checkUsernameAvailability(username, email, password)
        }
    }
    private fun checkUsernameAvailability(username: String, email: String, password: String) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Username is available, proceed with registration
                    createUser(username, email, password)
                } else {
                    // Username is already taken
                    errorLayout.error = "Username is not available."
                }
            }
            .addOnFailureListener { exception ->
                Log.w("RegisterFragment", "Error checking username availability.", exception)
                errorLayout.error = "Error checking username availability."
            }
    }
    private fun createUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Registration success
                    Log.d("RegisterFragment", "createUserWithEmail:success")
                    val user = auth.currentUser

                    // Update the user's display name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    user!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                Log.d("RegisterFragment", "User profile updated.")
                                // Add the username to Firestore
                                addUsernameToFirestore(username)
                            } else {
                                Log.w("RegisterFragment", "updateProfile:failure", profileTask.exception)
                                errorLayout.error = "Username could not be saved."
                            }
                        }
                    // Navigate to next fragment, or any other action needed.
                    //For example:
                    //findNavController().navigate(R.id.action_registerFragment_to_someOtherFragment)
                } else {
                    // Registration failed
                    Log.w("RegisterFragment", "createUserWithEmail:failure", task.exception)
                    errorLayout.error = "Registration failed."
                }
            }
    }

    private fun addUsernameToFirestore(username: String) {
        val usernameData = hashMapOf("username" to username)

        db.collection("users")
            .document(auth.currentUser!!.uid) // Use the user's UID as the document ID
            .set(usernameData)
            .addOnSuccessListener {
                Log.d("RegisterFragment", "Username added to Firestore.")
                Toast.makeText(
                    requireContext(),
                    "Registration success.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
            .addOnFailureListener { e ->
                Log.w("RegisterFragment", "Error adding username to Firestore", e)
                errorLayout.error = "Error saving username"
            }
    }
}