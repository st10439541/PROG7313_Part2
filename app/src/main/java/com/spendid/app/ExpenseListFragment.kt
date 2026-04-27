package com.spendid.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class ExpenseListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private lateinit var db: DatabaseHelper
    private lateinit var tvTotalExpenses: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_expense_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerExpenses)
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses)

        db = DatabaseHelper(requireContext())
        val expenseList = db.getAllExpenses()

        adapter = ExpenseAdapter(expenseList, requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Show the total right away
        updateTotal(expenseList)
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list and total every time the screen is shown
        val updatedList = db.getAllExpenses()
        adapter.refreshData(updatedList)
        updateTotal(updatedList)
    }

    private fun updateTotal(expenses: List<Expense>) {
        val total = expenses.sumOf { it.amount }
        val formatted = NumberFormat.getCurrencyInstance(Locale("en", "ZA")).format(total)
        tvTotalExpenses.text = formatted   // e.g. "R1,234.56"
    }
}