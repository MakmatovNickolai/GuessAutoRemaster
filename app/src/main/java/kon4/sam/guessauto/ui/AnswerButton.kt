package kon4.sam.guessauto.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import kon4.sam.guessauto.R


class AnswerButton constructor(context: Context, attrs: AttributeSet)
    : AppCompatButton(context, attrs) {

    companion object {
        val STATE_CORRECT_ANSWER = intArrayOf(R.attr.state_correct_answer)
        val STATE_HAS_ANSWER = intArrayOf(R.attr.state_has_answer)
    }

    private var isCorrectAnswer = false
    private var hasAnswer = false

    fun setIsCorrectAnswer(isCorrect: Boolean) {
        isCorrectAnswer = isCorrect
    }

    fun setHasAnswer(has: Boolean) {
        hasAnswer = has
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray? {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)
        if (isCorrectAnswer) {
            mergeDrawableStates(drawableState, STATE_CORRECT_ANSWER)
        }
        if (hasAnswer) {
            mergeDrawableStates(drawableState, STATE_HAS_ANSWER)
        }
        return drawableState
    }
}