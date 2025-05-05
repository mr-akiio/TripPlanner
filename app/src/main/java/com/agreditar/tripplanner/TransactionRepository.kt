package com.agreditar.tripplanner

import android.util.Log
import androidx.room.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TransactionRepository(private val db: FirebaseFirestore, private val auth: FirebaseAuth) {
    fun createTransaction(
        tripId: String,
        description: String,
        amount: Double,
        currency: String,
        date: String,
        payer: String,
        splitType: String,
        members: List<String>,
        onComplete: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return

        val transactionData = hashMapOf(
            "description" to description,
            "amount" to amount,
            "currency" to currency,
            "date" to date,
            "payer" to payer,
            "splitType" to splitType
        )

        db.collection("users").document(userId).collection("trips").document(tripId).collection("transactions")
            .add(transactionData)
            .addOnSuccessListener { documentReference ->
                Log.d("TransactionRepository", "Transaction added with ID: ${documentReference.id}")
                val transactionId = documentReference.id
                if (splitType == "individual"){
                    addIndividualAmount(userId, tripId, transactionId, payer, amount)
                    members.forEach { member->
                        if (member != payer){
                            addIndividualAmount(userId, tripId, transactionId, member, 0.0)
                        }
                    }
                }
                else if(splitType == "even") {
                    val amountPerUser = amount / members.size
                    members.forEach { member ->
                        addIndividualAmount(userId, tripId, transactionId, member, amountPerUser)
                    }
                }
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.w("TransactionRepository", "Error adding transaction", e)
                onFailure(e)
            }
    }
    private fun addIndividualAmount(
        userId: String,
        tripId: String,
        transactionId: String,
        memberId:String,
        amount: Double,
    ) {
        val individualAmount = hashMapOf(
            "amount" to amount,
        )
        db.collection("users").document(userId).collection("trips")
            .document(tripId).collection("transactions").document(transactionId)
            .collection("individualAmounts").document(memberId)
            .set(individualAmount)
            .addOnSuccessListener {
                Log.d("TransactionRepository", "Individual Amount added")
            }
            .addOnFailureListener { e ->
                Log.w("TransactionRepository", "Error adding Individual Amount", e)
            }
    }
    fun loadTransactionsFromFirestore(
        tripId: String,
        transactionList: MutableList<Transaction>,
        onComplete: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        transactionList.clear()
        db.collection("users").document(userId).collection("trips").document(tripId).collection("transactions")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val transaction = Transaction(
                        id = document.id,
                        description = document.getString("description") ?: "",
                        amount = document.getDouble("amount") ?: 0.0,
                        currency = document.getString("currency") ?: "",
                        date = document.getString("date") ?: "",
                        payer = document.getString("payer") ?: "",
                        splitType = document.getString("splitType") ?: ""
                    )
                    transactionList.add(transaction)
                    loadIndividualAmountsFromFirestore(userId, tripId, document.id, transactionList)
                }
                onComplete()
            }
            .addOnFailureListener { exception ->
                Log.w("TransactionRepository", "Error getting transactions: ", exception)
                onFailure(exception)
            }
    }
    fun loadIndividualAmountsFromFirestore(userId: String, tripId: String, transactionId: String, transactionList: MutableList<Transaction>) {
        db.collection("users").document(userId).collection("trips").document(tripId)
            .collection("transactions").document(transactionId)
            .collection("individualAmounts").get()
            .addOnSuccessListener { documents ->
                val individualAmounts = mutableMapOf<String, Double>()
                for (document in documents) {
                    val memberId = document.id
                    val amount = document.getDouble("amount") ?: 0.0
                    individualAmounts[memberId] = amount
                }
                // Find the transaction in the list and update its individualAmounts
                val transaction = transactionList.find { it.id == transactionId }
                transaction?.individualAmounts?.putAll(individualAmounts)
            }
            .addOnFailureListener { e ->
                Log.w("TransactionRepository", "Error getting individual amounts: ", e)
            }
    }
    fun updateTransaction(
        tripId: String,
        transactionId: String,
        description: String,
        amount: Double,
        currency: String,
        date: String,
        payer: String,
        onComplete: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        val transactionData = hashMapOf(
            "description" to description,
            "amount" to amount,
            "currency" to currency,
            "date" to date,
            "payer" to payer
        )
        db.collection("users").document(userId).collection("trips").document(tripId).collection("transactions").document(transactionId)
            .update(transactionData as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("TransactionRepository", "DocumentSnapshot successfully updated!")
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.w("TransactionRepository", "Error updating document", e)
                onFailure(e)
            }
    }
    fun deleteTransaction(
        tripId: String,
        transactionId: String,
        onComplete: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("trips").document(tripId).collection("transactions").document(transactionId)
            .delete()
            .addOnSuccessListener {
                Log.d("TransactionRepository", "DocumentSnapshot successfully deleted!")
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.w("TransactionRepository", "Error deleting document", e)
                onFailure(e)
            }
    }
}