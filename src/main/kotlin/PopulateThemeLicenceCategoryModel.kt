import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class PopulateThemeLicenceCategoryModel(
    @Contextual
    val id: String,
    val licenceCategoryId: String
)
