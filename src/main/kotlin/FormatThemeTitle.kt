import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

fun main() {
    val themeTitle = "ДОДАТКОВІ ПИТАННЯ ЩОДО КАТЕГОРІЇ Т (БЕЗПЕКА)"
    println(themeTitle.get(0) + themeTitle.toLowerCase().substring(1))
}