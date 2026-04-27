package com.spendid.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle View Report button click
        val btnViewReport = view.findViewById<MaterialButton>(R.id.btnViewReport)
        btnViewReport?.setOnClickListener {
            // Switch to Reports tab in MainActivity
            (activity as? MainActivity)?.navigateToReports()
        }

        // TODO: bind other views and set up dashboard logic (greeting, progress, recent expenses)
    }
}