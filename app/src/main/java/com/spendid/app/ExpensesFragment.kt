package com.spendid.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ExpensesFragment : Fragment() {

    private val expenseRepository by lazy {
        ExpenseRepository(DatabaseHelper(requireContext()))
    }

    private val categoryRepository by lazy {
        CategoryRepository(DatabaseHelper(requireContext()))
    }

    // MutableList so items can be removed
    private var allExpenses: MutableList<Expense> = mutableListOf()

    private lateinit var adapter: ExpenseAdapter
    private lateinit var recycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_expense_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.recyclerExpenses)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        //Convert list to MutableList
        allExpenses = expenseRepository.getAllExpenses().toMutableList()

        // Pass delete lambda to adapter
        adapter = ExpenseAdapter(allExpenses, categoryRepository) { expense, position ->

            // Delete from database
            expenseRepository.deleteExpense(expense.id)

            // Remove from local list
            allExpenses.removeAt(position)

            // Notify adapter
            adapter.notifyItemRemoved(position)

            // Update total after deletion
            val tvTotal = view.findViewById<TextView>(R.id.tvTotalExpenses)
            val updatedTotal = allExpenses.sumOf { it.amount }
            tvTotal.text = "R %.2f".format(updatedTotal)
        }

        recycler.adapter = adapter

        // Initial total display
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalExpenses)
        val total = allExpenses.sumOf { it.amount }
        tvTotal.text = "R %.2f".format(total)

        // Date label
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val sdf = java.text.SimpleDateFormat(
            "📅 MMMM yyyy",
            java.util.Locale.getDefault()
        )
        tvDate.text = sdf.format(java.util.Date())
    }
}