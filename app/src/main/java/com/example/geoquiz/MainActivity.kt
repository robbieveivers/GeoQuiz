package com.example.geoquiz

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val REQUEST_CODE_CHEAT = 0
private const val KEY_NUM_CORRECT = "num_correct"
private const val KEY_SCORE_VISIBLE = "score_visible"
private const val KEY_PLAY_AGAIN_VISIBLE = "play_again_visible"

class MainActivity : AppCompatActivity() {
    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button
    private lateinit var cheatButton: Button
    private lateinit var questionTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var playAgainButton: Button

    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProvider(this).get(QuizViewModel::class.java)
    }

    private var buttonState = true
    private var numCorrect = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        setContentView(R.layout.activity_main)

        // Initialize all views first
        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        cheatButton = findViewById(R.id.cheat_button)
        questionTextView = findViewById(R.id.question_text_view)
        prevButton = findViewById(R.id.prev_button)
        scoreTextView = findViewById(R.id.score_text_view)
        playAgainButton = findViewById(R.id.play_again_button)

        Log.d(TAG, "Got a QuizViewModel: $quizViewModel")

        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        quizViewModel.currentIndex = currentIndex

        numCorrect = savedInstanceState?.getFloat(KEY_NUM_CORRECT, 0f) ?: 0f
        val scoreVisible = savedInstanceState?.getBoolean(KEY_SCORE_VISIBLE, false) ?: false
        val playAgainVisible = savedInstanceState?.getBoolean(KEY_PLAY_AGAIN_VISIBLE, false) ?: false
        scoreTextView.visibility = if (scoreVisible) View.VISIBLE else View.GONE
        playAgainButton.visibility = if (playAgainVisible) View.VISIBLE else View.GONE
        if (scoreVisible) {
            val score = (numCorrect / quizViewModel.questionBankSize) * 100
            scoreTextView.text = getString(R.string.score, score.toInt())
            setViewIsEnabled(false, trueButton, falseButton, nextButton, prevButton, cheatButton)
        }


        trueButton.setOnClickListener {
            quizViewModel.setQuestionAnswered()
            setAnswerButtonState()
            checkAnswer(true)
        }
        falseButton.setOnClickListener {
            quizViewModel.setQuestionAnswered()
            setAnswerButtonState()
            checkAnswer(false)
        }
        nextButton.setOnClickListener {
            quizViewModel.moveToNext()
            updateQuestion()
            setAnswerButtonState()
        }
        prevButton.setOnClickListener {
            quizViewModel.moveToPrevious()
            updateQuestion()
            setAnswerButtonState()
        }
        questionTextView.setOnClickListener {
            quizViewModel.moveToNext()
            updateQuestion()
            setAnswerButtonState()
        }
        cheatButton.setOnClickListener { view ->
            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val options = ActivityOptions.makeClipRevealAnimation(view, 0,0,view.width,view.height)
                startActivityForResult(intent, REQUEST_CODE_CHEAT, options.toBundle())
            } else {
                startActivityForResult(intent, REQUEST_CODE_CHEAT)
            }
        }
        playAgainButton.setOnClickListener {
            resetQuiz()
        }
        updateQuestion()
        setAnswerButtonState()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        Log.i(TAG, "onSaveInstanceState")
        savedInstanceState.putInt(KEY_INDEX, quizViewModel.currentIndex)
        savedInstanceState.putFloat(KEY_NUM_CORRECT, numCorrect)
        savedInstanceState.putBoolean(KEY_SCORE_VISIBLE, scoreTextView.visibility == View.VISIBLE)
        savedInstanceState.putBoolean(KEY_PLAY_AGAIN_VISIBLE, playAgainButton.visibility == View.VISIBLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == REQUEST_CODE_CHEAT) {
            quizViewModel.isCheater = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN,false) ?: false
            if (quizViewModel.isCheater) {
                quizViewModel.setQuestionCheatStatus()
            }
        }
    }

    private fun updateQuestion() {
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)
    }

    private fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = quizViewModel.currentQuestionAnswer
        val messageResId = when {
            quizViewModel.didUserCheat() -> R.string.judgment_toast
            userAnswer == correctAnswer -> {
                numCorrect++
                R.string.correct_toast
            }
            else -> R.string.incorrect_toast
        }
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
        if (quizViewModel.isAllAnswered()) {
            val score = (numCorrect / quizViewModel.questionBankSize) * 100
            scoreTextView.text = getString(R.string.score, score.toInt())
            scoreTextView.visibility = View.VISIBLE
            playAgainButton.visibility = View.VISIBLE
            setViewIsEnabled(false, trueButton, falseButton, nextButton, prevButton, cheatButton)
        }
    }

    private fun setViewIsEnabled(state: Boolean, vararg views: View) {
        for (view in views) {
            view.isEnabled = state
        }
    }
    private fun disableButtons() {
        if (buttonState) {
            setViewIsEnabled(false, falseButton, trueButton)
            buttonState = false
        }
    }
    private fun enableButtons() {
        if (!buttonState) {
            setViewIsEnabled(true, falseButton, trueButton)
            buttonState = true
        }
    }
    private fun setAnswerButtonState() {
        if (quizViewModel.checkIfAnswered()) {
            disableButtons()
        } else {
            enableButtons()
        }
    }
    private fun resetQuiz() {
        quizViewModel.currentIndex = 0
        numCorrect = 0f
        // Reset all questions' answered and cheated status
        val field = quizViewModel.javaClass.getDeclaredField("questionBank")
        field.isAccessible = true
        val questions = field.get(quizViewModel) as List<Question>
        questions.forEach {
            it.isAnswered = false
            it.cheated = false
        }
        scoreTextView.visibility = View.GONE
        playAgainButton.visibility = View.GONE
        updateQuestion()
        setAnswerButtonState()
        setViewIsEnabled(true, trueButton, falseButton, nextButton, prevButton, cheatButton)
    }
}