package tech.uniapp.pdr.launch.domain.model

import PopulatedQuestionRange
import PopulateStageType
import kotlinx.serialization.Serializable

@Serializable
data class PopulateStageModel(
    val id: String,
    val themeId: String,
    val order: Int,
    val type: PopulateStageType = PopulateStageType.REGULAR,
    val questionRange: PopulatedQuestionRange?
)
