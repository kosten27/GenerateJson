import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class PopulateAnswerOptionModel(
    val id: String,
    val position: Int,
    val text: String,
    val isCorrect: Boolean
)
