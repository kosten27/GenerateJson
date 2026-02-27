
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class PopulateThemeModel(
    @Contextual
    val id: String,
    val title: String,
    val order: Int,
    val suborder: Int? = null,
    val isCommon: Boolean,
    val questionCount: Int? = null,
    val licenceCategoryList: List<PopulateThemeLicenceCategoryModel> = emptyList()
)
