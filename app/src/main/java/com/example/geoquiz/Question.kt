package com.example.geoquiz

import androidx.annotation.StringRes
import java.io.Serializable

data class Question(
    @StringRes val textResId: Int,
    val answer: Boolean,
    var isAnswered: Boolean = false,
    var cheated: Boolean = false
) : Serializable
