package com.spendid.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView

class Step3Fragment : Fragment() {

    private var selectedHabitId = R.id.cardInControl
    private var selectedHabit = "Mostly in control"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_tutorial_step3, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cards = listOf(
            view.findViewById<CardView>(R.id.cardDisciplined),
            view.findViewById<CardView>(R.id.cardInControl),
            view.findViewById<CardView>(R.id.cardNeedsImprovement),
            view.findViewById<CardView>(R.id.cardOverspend)
        )

        cards.forEach { card ->
            card.setOnClickListener {
                updateSelection(card.id)
                selectedHabit = when (card.id) {
                    R.id.cardDisciplined -> "Very disciplined"
                    R.id.cardInControl -> "Mostly in control"
                    R.id.cardNeedsImprovement -> "Needs improvement"
                    R.id.cardOverspend -> "Often overspend"
                    else -> "Mostly in control"
                }
            }
        }
    }

    private fun updateSelection(selectedId: Int) {
        selectedHabitId = selectedId
        val cards = listOf(
            requireView().findViewById<CardView>(R.id.cardDisciplined),
            requireView().findViewById<CardView>(R.id.cardInControl),
            requireView().findViewById<CardView>(R.id.cardNeedsImprovement),
            requireView().findViewById<CardView>(R.id.cardOverspend)
        )
        cards.forEach { card ->
            if (card.id == selectedId) {
                card.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_card_option_selected)
            } else {
                card.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_card_option_unselected)
            }
        }
    }

    fun getSelectedHabit(): String = selectedHabit
}