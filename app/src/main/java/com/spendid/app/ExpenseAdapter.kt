package com.spendid.app

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpenseAdapter(
    private val list: MutableList<Expense>,
    private val categoryRepository: CategoryRepository,
    private val onDelete: (Expense, Int) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.expenseTitle)
        val meta: TextView = view.findViewById(R.id.expenseMeta)
        val amount: TextView = view.findViewById(R.id.expenseAmount)
        val dateTime: TextView = view.findViewById(R.id.tvDateTime)
        val image: ImageView = view.findViewById(R.id.imgExpense)
        val deleteButton: ImageView = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        val category = categoryRepository.getCategoryById(item.categoryId)
        val categoryName = category?.name ?: "Unknown"
        val categoryIcon = category?.icon ?: "📦"

        holder.title.text = item.description
        holder.meta.text = "$categoryIcon $categoryName • ${item.date}"
        holder.amount.text = "R ${String.format("%.2f", item.amount)}"
        holder.dateTime.text = "${item.date} | ${item.time}"

        // Receipt image
        if (!item.imageUri.isNullOrEmpty()) {
            holder.image.setImageURI(Uri.parse(item.imageUri))
            holder.image.visibility = View.VISIBLE
        } else {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery)
            holder.image.visibility = View.VISIBLE
        }

        holder.deleteButton.setOnClickListener {
            onDelete(item, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = list.size
}