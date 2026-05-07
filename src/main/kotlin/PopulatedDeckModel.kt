import kotlinx.serialization.Serializable

@Serializable
data class PopulatedDeckModel(
    val deckId: String = "",
    val words: List<PopulatedWordModel> = emptyList()
)
