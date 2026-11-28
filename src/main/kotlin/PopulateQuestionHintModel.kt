import kotlinx.serialization.Serializable

@Serializable
data class PopulateQuestionHintModel(
    val id: String,
    val trafficRuleSectionId: String,
    val position: Int
)
