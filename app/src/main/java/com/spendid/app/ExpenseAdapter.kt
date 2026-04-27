package com.spendid.app

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val context: Context
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val db = DatabaseHelper(context)

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgExpense: ImageView = itemView.findViewById(R.id.imgExpense)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        holder.tvTitle.text = expense.title
        holder.tvAmount.text = "R ${String.format("%.2f", expense.amount)}"
        holder.tvCategory.text = expense.category
        holder.tvDateTime.text = "${expense.date} | ${expense.startTime} - ${expense.endTime}"

        // Handle image
        if (expense.imagePath.isNotEmpty()) {
            try {
                holder.imgExpense.setImageURI(Uri.parse(expense.imagePath))
            } catch (e: Exception) {
                holder.imgExpense.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.imgExpense.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.deleteButton.setOnClickListener {
            db.deleteExpense(expense.id)
            refreshData(db.getAllExpenses())
            Toast.makeText(holder.itemView.context, "Expense deleted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = expenses.size

    fun refreshData(newList: List<Expense>) {
        expenses = newList
        notifyDataSetChanged()
    }
}