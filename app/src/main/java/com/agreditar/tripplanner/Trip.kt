package com.agreditar.tripplanner

data class Trip(
    val id: String = "", // Unique ID (optional)
    val destination: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val details: String = "",
)