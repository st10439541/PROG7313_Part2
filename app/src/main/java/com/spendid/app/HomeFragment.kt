package com.spendid.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class HomeFragment private constructor() : Fragment() {

    private var username: String = ""

    companion object {
        fun newInstance(username: String): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString("USERNAME", username)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments?.getString("USERNAME") ?: "User"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set username in greeting
        val userNameText = view.findViewById<TextView>(R.id.userName)
        userNameText?.text = username

        // Handle View Report button click
        val btnViewReport = view.findViewById<MaterialButton>(R.id.btnViewReport)
        btnViewReport?.setOnClickListener {
            (activity as? MainActivity)?.navigateToReports()
        }

        // TODO: bind other views and set up dashboard logic (greeting, progress, recent expenses)
    }
}