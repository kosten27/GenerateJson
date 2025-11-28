
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class PopulateSectionModel(
    @Contextual
    val id: String,
    val title: String,
    val order: Int,
    val suborder: Int? = null,
    val isCommon: Boolean,
    val licenceCategoryList: List<PopulateSectionLicenceCategoryModel> = emptyList()
)
