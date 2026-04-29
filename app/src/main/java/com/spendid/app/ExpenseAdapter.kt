package com.spendid.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpenseAdapter(private val list: List<Expense>, private val categoryRepository: CategoryRepository) :
    RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val title: TextView = view.findViewById(R.id.expenseTitle)
        val meta: TextView = view.findViewById(R.id.expenseMeta)
        val amount: TextView = view.findViewById(R.id.expenseAmount)
        val icon: TextView = view.findViewById(R.id.expenseIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        // Get category name and icon from categoryId
        val category = categoryRepository.getCategoryById(item.categoryId)
        val categoryName = category?.name ?: "Unknown"
        val categoryIcon = category?.icon ?: "📦"

        holder.icon.text = categoryIcon
        holder.title.text = item.description
        holder.meta.text = "$categoryName • ${item.date}"
        holder.amount.text = "R ${String.format("%.2f", item.amount)}"
    }

    override fun getItemCount(): Int = list.size
}