package com.example.geoquiz

import androidx.lifecycle.ViewModel

class QuizViewModel : ViewModel() {
    private var questionBank = listOf(
        Question(R.string.question_australia, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_mideast, false),
        Question(R.string.question_africa, false),
        Question(R.string.question_americas, true),
        Question(R.string.question_asia, true)
    )
    var currentIndex = 0
    var isCheater = false
    val questionBankSize: Int
        get() = questionBank.size
    val currentQuestionAnswer: Boolean
        get() = questionBank[currentIndex].answer
    val currentQuestionText: Int
        get() = questionBank[currentIndex].textResId
    fun moveToNext() {
        currentIndex = (currentIndex + 1) % questionBank.size
    }
    fun moveToPrevious() {
        currentIndex = if (currentIndex == 0) questionBank.size - 1 else currentIndex - 1
    }
    fun isAllAnswered(): Boolean {
        for (question in questionBank) {
            if (!question.isAnswered) return false
        }
        return true
    }
    fun setQuestionAnswered() {
        val temp = questionBank.toMutableList()
        temp[currentIndex].isAnswered = true
        questionBank = temp
    }
    fun checkIfAnswered(): Boolean {
        return questionBank[currentIndex].isAnswered
    }
    fun didUserCheat(): Boolean {
        return questionBank[currentIndex].cheated
    }
    fun setQuestionCheatStatus() {
        val temp = questionBank.toMutableList()
        temp[currentIndex].cheated = true
        questionBank = temp
    }
}