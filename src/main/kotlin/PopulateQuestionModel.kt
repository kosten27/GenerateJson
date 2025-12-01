package tech.uniapp.pdr.launch.domain.model

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class PopulateQuestionModel(
    val id: String,
    val sectionId: String,
    val order: Int,
    val text: String,
    val imageResId: String? = null,
    val answerOptionList: List<PopulateAnswerOptionModel>
)
