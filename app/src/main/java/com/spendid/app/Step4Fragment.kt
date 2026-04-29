package com.spendid.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial

class Step4Fragment : Fragment() {

    private lateinit var switchAlerts: SwitchMaterial
    private lateinit var switchReminder: SwitchMaterial
    private lateinit var switchBadges: SwitchMaterial
    private lateinit var switchDarkMode: SwitchMaterial

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_tutorial_step4, container, false)

        switchAlerts = view.findViewById(R.id.switchAlerts)
        switchReminder = view.findViewById(R.id.switchReminder)
        switchBadges = view.findViewById(R.id.switchBadges)
        switchDarkMode = view.findViewById(R.id.switchDarkMode)

        return view
    }

    fun getBudgetAlerts(): Boolean = switchAlerts.isChecked
    fun getDailyReminder(): Boolean = switchReminder.isChecked
    fun getBadgeNotifications(): Boolean = switchBadges.isChecked
    fun getDarkMode(): Boolean = switchDarkMode.isChecked
}