import kotlinx.serialization.Serializable

@Serializable
enum class PopulateStageType {
    REGULAR,
    MISTAKES_REVIEW
}