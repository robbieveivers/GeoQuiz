package com.example.geoquiz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

private const val EXTRA_ANSWER_IS_TRUE = "com.example.geoquiz.answer_is_true"
const val EXTRA_ANSWER_SHOWN = "com.example.geoquiz.answer_shown"
private const val KEY_ANSWER_SHOWN = "answer_shown"
private const val KEY_ANSWER_IS_TRUE = "answer_is_true"

class CheatActivity : AppCompatActivity() {
    private var answerIsTrue = false;
    private lateinit var answerTextView: TextView
    private lateinit var showAnswerButton: Button
    private var isAnswerShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cheat)

        answerTextView = findViewById(R.id.answer_text_view)
        showAnswerButton = findViewById(R.id.show_answer_button)

        answerIsTrue = savedInstanceState?.getBoolean(KEY_ANSWER_IS_TRUE)
            ?: intent.getBooleanExtra(EXTRA_ANSWER_IS_TRUE, false)
        isAnswerShown = savedInstanceState?.getBoolean(KEY_ANSWER_SHOWN, false) ?: false
        if (isAnswerShown) {
            val answerText = if (answerIsTrue) R.string.true_button else R.string.false_button
            answerTextView.setText(answerText)
            setAnswerShownResult(true)
        }

        showAnswerButton.setOnClickListener {
            val answerText = when {
                answerIsTrue -> R.string.true_button
                else -> R.string.false_button
            }

            answerTextView.setText(answerText)
            setAnswerShownResult(true)
            isAnswerShown = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_ANSWER_SHOWN, isAnswerShown)
        outState.putBoolean(KEY_ANSWER_IS_TRUE, answerIsTrue)
    }

    companion object {
        fun newIntent(packageContext: Context, answerIsTrue: Boolean): Intent {
            return Intent(packageContext, CheatActivity::class.java).apply {
                putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue)
            }
        }
    }

    private fun setAnswerShownResult(isAnswerShown: Boolean) {
        val data = Intent().apply {
            putExtra(EXTRA_ANSWER_SHOWN, isAnswerShown)
        }
        setResult(Activity.RESULT_OK, data)
    }
}