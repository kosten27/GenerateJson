import kotlinx.serialization.json.Json
import java.io.File

fun main() {

    val questionHintsString = File("src/main/resources/question_hints.json").readText()
    val questionHintsList = Json.decodeFromString<List<PopulateQuestionHintModel>>(questionHintsString)

    val questionHintsMatchString = parseQuestionImageTypes(File("src/main/resources/question_hints_match.txt").readText())

    val themeQuestionFiles = File("src/main/resources/questions_theme_files.txt").readText().split("\n")
    themeQuestionFiles.forEach { themeQuestionFile ->
        val themeQuestionText = File("src/main/resources/$themeQuestionFile").readText()
        val questionList = Json.decodeFromString<List<PopulateQuestionModel>>(themeQuestionText)

        questionList.forEach { question ->
//            question.
        }
    }


    println("End")

}