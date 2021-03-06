/*
 * AccessComplete, an easy to use editor of accessibility related
 * OpenStreetMap data for Android.  This program is a fork of
 * StreetComplete (https://github.com/westnordost/StreetComplete).
 *
 * Copyright (C) 2020 Sven Stoll (AccessComplete author)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.uzh.ifi.accesscomplete.quests

import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import ch.uzh.ifi.accesscomplete.R
import ch.uzh.ifi.accesscomplete.quests.AbstractQuestAnswerFragment.Listener.SidewalkSide
import kotlinx.android.synthetic.main.fragment_quest_answer.*

/**
 * Abstract base class for dialogs that need to handle sidewalk quests. If the the OSM element of
 * the quest is annotated with the sidewalk tag, it can ask the question for both the left and
 * the right side (if they are available).
 */
abstract class AbstractQuestFormAnswerWithSidewalkSupportFragment<T> : AbstractQuestFormAnswerFragment<T>() {

    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    protected var elementHasSidewalk = false
    protected var sidewalkOnBothSides = false
    protected var currentSidewalkSide: SidewalkSide? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (shouldTagBySidewalkSideIfApplicable()) {
            handlePossibleSidewalks()
        }
    }

    protected open fun shouldTagBySidewalkSideIfApplicable(): Boolean = false

    private fun handlePossibleSidewalks() {
        if (osmElement == null) {
            return
        }

        val sidewalkTag = osmElement!!.tags["sidewalk"]
        if (sidewalkTag == null) {
            return
        }

        when {
            sidewalkTag.contentEquals("both") -> {
                sidewalkOnBothSides = true
                currentSidewalkSide = SidewalkSide.LEFT
                okButton.text = resources.getText(R.string.next)
            }
            sidewalkTag.contentEquals("left") -> {
                currentSidewalkSide = SidewalkSide.LEFT
            }
            sidewalkTag.contentEquals("right") -> {
                currentSidewalkSide = SidewalkSide.RIGHT
            }
        }

        if (currentSidewalkSide != null) {
            elementHasSidewalk = true
            listener?.onHighlightSidewalkSide(questId, questGroup, currentSidewalkSide!!)
        }
    }

    protected fun switchToOppositeSidewalkSide() {
        if (currentSidewalkSide == null) {
            return
        }

        bottomSheetContainer.startAnimation(
            AnimationUtils.loadAnimation(context, R.anim.enter_from_right)
        )

        currentSidewalkSide =
            if (currentSidewalkSide == SidewalkSide.LEFT) SidewalkSide.RIGHT
            else SidewalkSide.LEFT
        listener?.onHighlightSidewalkSide(questId, questGroup, currentSidewalkSide!!)

        okButton.text = resources.getText(android.R.string.ok)
        resetInputs()
    }

    protected open fun resetInputs() {
        // NOP
    }

    override fun addOtherAnswersDynamically(): List<OtherAnswer> {
        val sidewalkMappedSeparatelyAnswer = getSidewalkMappedSeparatelyAnswer()
        if (sidewalkMappedSeparatelyAnswer != null && currentSidewalkSide == null) {
            return emptyList()
        }

        return listOf(OtherAnswer(R.string.answer_sidewalk_is_mapped_separately) {
            confirmSeparatelyMappedSidewalk(sidewalkMappedSeparatelyAnswer!!)
        })
    }

    protected open fun getSidewalkMappedSeparatelyAnswer() : T? {
        return null
    }

    protected fun confirmSeparatelyMappedSidewalk(separatelyMappedAnswer: T) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(separatelyMappedAnswer) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
