package com.spendid.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class ExpensesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_expense_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //  View Report button click
        val btnViewReport = view.findViewById<MaterialButton>(R.id.btnViewReport)
        btnViewReport?.setOnClickListener {
            (activity as? MainActivity)?.navigateToReports()
        }


        val btnSetGoals = view.findViewById<MaterialButton>(R.id.btnSetGoals)
        btnSetGoals?.setOnClickListener {

        }
    }
}