import kotlinx.serialization.Serializable

@Serializable
data class PopulatedPdrHintModel(
    val id: String,
    val pdrId: String,
    val value: String
)
