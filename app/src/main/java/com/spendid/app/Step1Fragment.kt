package com.spendid.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment

class Step1Fragment : Fragment() {

    private lateinit var nameInput: EditText
    private lateinit var salaryInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_tutorial_step1, container, false)

        nameInput = view.findViewById(R.id.nameInput)
        salaryInput = view.findViewById(R.id.salaryInput)

        return view
    }

    fun getUserName(): String {
        return nameInput.text.toString().trim()
    }

    fun getSalary(): Double {
        val salaryText = salaryInput.text.toString().trim()
        return if (salaryText.isNotEmpty()) salaryText.toDoubleOrNull() ?: 0.0 else 0.0
    }
}