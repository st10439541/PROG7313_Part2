// ProfileFragment.kt
package com.spendid.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class ProfileFragment private constructor() : Fragment() {

    private var username: String = ""

    companion object {
        fun newInstance(username: String): ProfileFragment {
            val fragment = ProfileFragment()
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
        return inflater.inflate(R.layout.activity_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set username in profile header
        val profileName = view.findViewById<TextView>(R.id.profileName)
        profileName?.text = username

        // Generate a default email based on username
        val profileEmail = view.findViewById<TextView>(R.id.profileEmail)
        val email = "${username.lowercase().replace(" ", "")}@email.com"
        profileEmail?.text = email

        // Handle Logout button click
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        Toast.makeText(activity, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }
}