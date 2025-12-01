
fun main() {
    val answers = "344332133332"
    val formattedAnswers = answers.mapIndexed { index, c -> "${index+1}-$c" }.joinToString(",")
    println(formattedAnswers)
}