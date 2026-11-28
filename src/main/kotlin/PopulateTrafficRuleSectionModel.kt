import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class PopulateTrafficRuleSectionModel(
    val id: String,
    val content: String
)
