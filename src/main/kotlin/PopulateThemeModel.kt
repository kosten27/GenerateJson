
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class PopulateThemeModel(
    @Contextual
    val id: String,
    val title: String,
    val order: Int,
    val suborder: Int? = null,
    val isCommon: Boolean,
    val questionCount: Int? = null,
    val licenseCategoryGroupId: String? = null
)
