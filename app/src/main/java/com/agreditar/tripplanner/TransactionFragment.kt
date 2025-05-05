package com.agreditar.tripplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class TransactionFragment : Fragment() {

    private lateinit var usersChipGroup: ChipGroup
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var addTransactionButton: Button
    private lateinit var transactionNameEditText: TextInputEditText
    private lateinit var transactionAmountEditText: TextInputEditText
    private lateinit var transactionDateEditText: TextInputEditText
    private lateinit var splitOptionsChipGroup: ChipGroup
    private lateinit var splitEvenlyChip: Chip
    private lateinit var setIndividuallyChip: Chip
    private val selectedUsers = mutableListOf<UserPayment>()
    private lateinit var adapter: UserPaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)

        usersChipGroup = view.findViewById(R.id.usersChipGroup)
        usersRecyclerView = view.findViewById(R.id.usersRecyclerView)
        addTransactionButton = view.findViewById(R.id.addTransactionButton)
        transactionNameEditText = view.findViewById(R.id.transactionName)
        transactionAmountEditText = view.findViewById(R.id.transactionAmount)
        transactionDateEditText = view.findViewById(R.id.transactionDate)
        splitOptionsChipGroup = view.findViewById(R.id.splitOptionsChipGroup)
        splitEvenlyChip = view.findViewById(R.id.splitEvenlyChip)
        setIndividuallyChip = view.findViewById(R.id.setIndividuallyChip)

        // Sample users (replace with your actual user list)
        val allUsers = listOf(
            UserPayment("user1", "Alice", 0.0),
            UserPayment("user2", "Bob", 0.0),
            UserPayment("user3", "Charlie", 0.0),
            UserPayment("user4", "David", 0.0)
        )

        // Create and add chips
        allUsers.forEach { user ->
            val chip = Chip(requireContext()).apply {
                text = user.userName
                isCheckable = true
                isClickable = true
                setOnClickListener {
                    if (isChecked) {
                        selectedUsers.add(UserPayment(user.userId, user.userName, 0.0))
                    } else {
                        selectedUsers.removeIf { it.userId == user.userId }
                    }
                    updateRecyclerViewVisibility()
                    if (splitEvenlyChip.isChecked) {
                        calculateEvenSplit()
                    }
                }
            }
            usersChipGroup.addView(chip)
        }

        // RecyclerView setup
        adapter = UserPaymentAdapter(selectedUsers, splitEvenlyChip.isChecked)
        usersRecyclerView.adapter = adapter
        usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Split options chip group
        splitOptionsChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            when {
                R.id.splitEvenlyChip in checkedIds -> {
                    calculateEvenSplit()
                    updateRecyclerViewVisibility()
                    adapter.updateSplitType(true)
                }

                R.id.setIndividuallyChip in checkedIds -> {
                    updateRecyclerViewVisibility()
                    adapter.updateSplitType(false)
                }
            }

        }

        // Add transaction button click listener
        addTransactionButton.setOnClickListener {
            addTransaction()
        }
        updateRecyclerViewVisibility()
        return view
    }

    private fun addTransaction() {
        val transactionName = transactionNameEditText.text.toString()
        val transactionAmount = transactionAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
        val transactionDate = transactionDateEditText.text.toString()

        // Validate data (add more validation as needed)
        if (transactionName.isEmpty()) {
            transactionNameEditText.error = "Transaction name is required"
            return
        }
        if (selectedUsers.isEmpty()) {
            Toast.makeText(requireContext(), "Select at least one user", Toast.LENGTH_SHORT).show()
            return
        }
        // Here you would save the transaction data to your backend (e.g., Firestore)
        // ...

        // Clear fields and selected users
        transactionNameEditText.text?.clear()
        transactionAmountEditText.text?.clear()
        transactionDateEditText.text?.clear()

        selectedUsers.clear()
        adapter.notifyDataSetChanged()
        // Uncheck all chips
        for (i in 0 until usersChipGroup.childCount) {
            val chip = usersChipGroup.getChildAt(i) as Chip
            chip.isChecked = false
        }
    }
    private fun calculateEvenSplit() {
        val totalAmount = transactionAmountEditText.text.toString().toDoubleOrNull() ?: 0.0
        val numberOfUsers = selectedUsers.size

        if (numberOfUsers > 0) {
            val amountPerUser = totalAmount / numberOfUsers
            selectedUsers.forEach { it.amount = amountPerUser }
            adapter.notifyDataSetChanged()
        }
    }

    private fun updateRecyclerViewVisibility() {
        if (selectedUsers.isEmpty()) {
            usersRecyclerView.visibility = View.GONE
        } else {
            usersRecyclerView.visibility = View.VISIBLE
        }
    }
}

class UserPaymentAdapter(private val userPayments: MutableList<UserPayment>, private var splitEvenly: Boolean) :
    RecyclerView.Adapter<UserPaymentAdapter.UserPaymentViewHolder>() {

    class UserPaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val userAmountEditText: TextInputEditText = itemView.findViewById(R.id.userAmountEditText)
        val userAmountLayout: TextInputLayout = itemView.findViewById(R.id.userAmountLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPaymentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_payment, parent, false)
        return UserPaymentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserPaymentViewHolder, position: Int) {
        val userPayment = userPayments[position]
        holder.userNameTextView.text = userPayment.userName
        if (splitEvenly) {
            holder.userAmountEditText.isEnabled = false
            holder.userAmountLayout.hint = "Amount"
        } else {
            holder.userAmountEditText.isEnabled = true
            holder.userAmountLayout.hint = "Amount"
        }
        holder.userAmountEditText.setText(userPayment.amount.toString())
        holder.userAmountEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val amountText = holder.userAmountEditText.text.toString()
                val amount = amountText.toDoubleOrNull() ?: 0.0
                userPayment.amount = amount
            }
        }
        holder.userAmountLayout.setEndIconOnClickListener {
            holder.userAmountEditText.requestFocus()
            holder.userAmountEditText.text?.clear()
        }
    }
    fun updateSplitType(splitEvenly: Boolean){
        this.splitEvenly = splitEvenly
        notifyDataSetChanged()
    }

    override fun getItemCount() = userPayments.size
}