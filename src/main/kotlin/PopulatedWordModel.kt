import kotlinx.serialization.Serializable

@Serializable
data class PopulatedWordModel(
    val id: String,
    val value: String
)
