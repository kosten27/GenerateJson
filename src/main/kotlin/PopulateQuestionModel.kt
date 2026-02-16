import kotlinx.serialization.Serializable

@Serializable
data class PopulateQuestionModel(
    val id: String,
    val themeId: String,
    val sourceOrder: Int? = null,
    val order: Int,
    val text: String,
    val imageResId: String? = null,
    val imageType: PopulatedImageType? = null,
    val answerOptionList: List<PopulateAnswerOptionModel>
)
