
fun main() {
    val firstImageName = 173
    val questionImages = "1-17"
    val questions = questionImages.split(",")
        .flatMap { part ->
            if (part.contains("-")) {
                val (start, end) = part.split("-").map { it.toInt() }
                (start..end).toList()
            } else {
                listOf(part.toInt())
            }
        }
    var nextImageName = firstImageName
    val result = questions.map { questionNumber ->
        "${questionNumber}-${nextImageName++}"
    }.joinToString(",")
    println(result)
}