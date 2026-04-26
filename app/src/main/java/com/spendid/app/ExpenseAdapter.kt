package com.spendid.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(private val expenses: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(android.R.id.text1)
        val timeText: TextView = itemView.findViewById(android.R.id.text2)
        val descText: TextView = itemView.findViewById(android.R.id.summary)
//        val amountText: TextView = itemView.findViewById(android.R.id.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.dateText.text = expense.date
        holder.timeText.text = "${expense.timeStart} - ${expense.timeEnd}" ?: "No set time"
        holder.descText.text = expense.description ?: "No description"
//        holder.amountText.text = expense.amount?.let { "R ${String.format("%.2f", it)}" } ?: "No amount"
    }

    override fun getItemCount() = expenses.size
}