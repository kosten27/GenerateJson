import kotlinx.serialization.Serializable

@Serializable
data class PopulatedQuestionRange(
    val from: Int,
    val to: Int
)
