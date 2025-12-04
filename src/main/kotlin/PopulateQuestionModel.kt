package tech.uniapp.pdr.launch.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PopulateQuestionModel(
    val id: String,
    val themeId: String,
    val order: Int,
    val text: String,
    val imageResId: String? = null,
    val answerOptionList: List<PopulateAnswerOptionModel>
)
