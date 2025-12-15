
import kotlinx.serialization.Serializable

@Serializable
data class PopulateStageModel(
    val id: String,
    val themeId: String,
    val order: Int,
    val type: PopulateStageType? = null,
    val questionRange: PopulatedQuestionRange?
)
