package com.spendid.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView

class Step2Fragment : Fragment() {

    private var selectedGoalId = R.id.cardSaveMoney

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_tutorial_step2, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cards = listOf(
            view.findViewById<CardView>(R.id.cardSaveMoney),
            view.findViewById<CardView>(R.id.cardTrackSpending),
            view.findViewById<CardView>(R.id.cardReachGoal),
            view.findViewById<CardView>(R.id.cardHousehold)
        )

        cards.forEach { card ->
            card.setOnClickListener {
                updateSelection(card.id)
            }
        }
    }

    private fun updateSelection(selectedId: Int) {
        selectedGoalId = selectedId
        val cards = listOf(
            requireView().findViewById<CardView>(R.id.cardSaveMoney),
            requireView().findViewById<CardView>(R.id.cardTrackSpending),
            requireView().findViewById<CardView>(R.id.cardReachGoal),
            requireView().findViewById<CardView>(R.id.cardHousehold)
        )
        cards.forEach { card ->
            if (card.id == selectedId) {
                card.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_card_option_selected)
            } else {
                card.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_card_option_unselected)
            }
        }
    }
}