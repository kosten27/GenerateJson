
fun main() {
    val firstImageName = 840
    val questionImages = "339-357"
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