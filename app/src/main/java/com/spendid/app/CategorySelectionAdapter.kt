package com.spendid.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CategorySelectionAdapter(
    context: Context,
    private val categories: List<Category>
) : ArrayAdapter<Category>(context, android.R.layout.simple_spinner_item, categories) {

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            android.R.layout.simple_spinner_item, parent, false
        )
        val textView = view as TextView
        val category = getItem(position)
        textView.text = "${category?.icon} ${category?.name}"
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            android.R.layout.simple_spinner_dropdown_item, parent, false
        )
        val textView = view as TextView
        val category = getItem(position)
        textView.text = "${category?.icon} ${category?.name}"
        return view
    }
}