package com.agreditar.tripplanner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userNameTextView: TextView
    private lateinit var tripRecyclerView: RecyclerView
    private lateinit var tripAdapter: TripAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private val tripList = mutableListOf<Trip>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        db = Firebase.firestore

        // Get references to UI elements
        userNameTextView = view.findViewById(R.id.userNameTextView)
        tripRecyclerView = view.findViewById(R.id.tripRecyclerView)
        bottomNavigationView = view.findViewById(R.id.bottomNavigationView)

        // Set up RecyclerView
        tripAdapter = TripAdapter(tripList) { trip ->
            // Handle item click here
            navigateToTripDetails(trip) // Navigate to the details
        }
        tripRecyclerView.adapter = tripAdapter
        tripRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Get the current user
        val user = auth.currentUser
        if (user != null) {
            // Set user's display name
            userNameTextView.text = user.displayName

            // Load trips from Firestore
            loadTripsFromFirestore()
        }

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.notificationsFragment -> {
                    findNavController().navigate(R.id.action_homeFragment_to_notificationsFragment)
                    true
                }
                R.id.addTripFragment -> {
                    findNavController().navigate(R.id.action_homeFragment_to_newTripFragment)
                    true
                }
                R.id.profileFragment -> {
                    findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                    true
                }
                else -> false
            }
        }

        return view
    }

    private fun loadTripsFromFirestore() {
        // Clear the existing trip list
        tripList.clear()

        // Get the current user's UID
        val userId = auth.currentUser?.uid ?: return // If no user is logged in, exit early

        // Reference to the user's trips collection
        db.collection("users").document(userId).collection("trips")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val trip = Trip(
                        id = document.id,
                        destination = document.getString("destination") ?: "",
                        startDate = document.getString("startDate") ?: "",
                        endDate = document.getString("endDate") ?: "",
                        details = document.getString("details") ?: "",
                    )
                    tripList.add(trip)
                    Log.d("HomeFragment", "Trip added")
                }
                tripAdapter.notifyDataSetChanged()
                Log.d("HomeFragment", "Trips loaded successfully")
            }
            .addOnFailureListener { exception ->
                Log.w("HomeFragment", "Error getting trips: ", exception)
            }
    }

    private fun navigateToTripDetails(trip: Trip) {
        // Create a bundle to pass the trip data
        val bundle = Bundle().apply {
            putString("tripId", trip.id)
            putString("tripDestination", trip.destination)
            putString("tripStartDate", trip.startDate)
            putString("tripEndDate", trip.endDate)
            putString("tripDetails", trip.details)
        }

        // Navigate to ShowTripFragment and pass the bundle
        findNavController().navigate(R.id.action_homeFragment_to_showTripFragment, bundle)
    }
}