import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class PopulateSectionLicenceCategoryModel(
    @Contextual
    val id: String,
    val licenceCategoryId: String
)
