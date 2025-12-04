@file:OptIn(ExperimentalUuidApi::class)

package org.example

import PopulateThemeModel
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import tech.uniapp.pdr.launch.domain.model.PopulateAnswerOptionModel
import tech.uniapp.pdr.launch.domain.model.PopulateQuestionModel
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@OptIn(ExperimentalUuidApi::class, ExperimentalSerializationApi::class)
fun main() {
    val themeJsonString = File("src/main/resources/themes.json").readText()
    val themeList = Json.decodeFromString<List<PopulateThemeModel>>(themeJsonString)

    val questionIds = mutableMapOf<String, Uuid>()
    val questionOptionIds = mutableMapOf<String, Uuid>()
    val themeQuestionFiles = File("src/main/resources/questions_theme_files.txt").readText().split("\n")
    themeQuestionFiles.forEach { themeQuestionFile ->
        val themeQuestionText = File("src/main/resources/$themeQuestionFile").readText()
        val questionList = Json.decodeFromString<List<PopulateQuestionModel>>(themeQuestionText)
        val currentThemeQuestionIds = questionList.associate { question ->
            val theme = themeList.find { theme ->
                theme.id == question.themeId
            }
            val themeNumber = theme?.let {
                it.order.toString() + it.suborder?.let { suborder -> ".$suborder" }.orEmpty()
            }
            "${themeNumber}_${question.order}" to Uuid.parse(question.id)
        }
        questionIds.putAll(currentThemeQuestionIds)

        questionList.forEach { question ->
            val theme = themeList.find { theme ->
                theme.id == question.themeId
            }
            val themeNumber = theme?.let {
                it.order.toString() + it.suborder?.let { suborder -> ".$suborder" }.orEmpty()
            }
            val questionIdKey = "${themeNumber}_${question.order}"
            val currentQuestionAnswerOptionIds = question.answerOptionList.associate { answerOption ->
                "${questionIdKey}_${answerOption.position}" to Uuid.parse(answerOption.id)
            }
            questionOptionIds.putAll(currentQuestionAnswerOptionIds)
        }
    }

    val themeWithImagesString = parseQuestionsWithImages(File("src/main/resources/question_images.txt").readText())
    val answersString = File("src/main/resources/answers.txt").readText().split("\n")

    val answerThemeRegex = Regex("""^(\d*.\d*):(.+)""")
    val answerPairRegex = Regex("""(\d+)-(\d+)""")
    val answerMap: MutableMap<String, Map<Int, Int>> = mutableMapOf()
    answersString.forEach { line ->
        if (answerThemeRegex.matches(line)) {
            val matchResult = answerThemeRegex.find(line)!!
            val themeId = matchResult.groupValues[1]
            val themeAnswers = answerPairRegex.findAll(matchResult.groupValues[2]).map {
                val (a, b) = it.destructured
                a.toInt() to b.toInt()
            }.toMap()
            answerMap[themeId] = themeAnswers
        }
    }

    val themes = mutableListOf<Theme>()
    var currentTheme: Theme? = null
    var currentThemeWithImages: ThemeImages? = null
    var currentQuestion: Question? = null

    val themeRegex = Regex("""^(\d+(.\d)?)\.\s+([A-ZА-ЯІЇЄҐ0-9\s'’\-(),]+)$""")
    val questionRegex = Regex("""^(\d+)\.\s+(.+)$""")
    val answerRegex = Regex("""^(\d+)\)\s+(.+)$""")

    val text = File("src/main/resources/pdr.txt").readText()
    text.lines().forEach {line ->
        val trimmed = line.trim()

        when {
            // THEME
            themeRegex.matches(trimmed) -> {
                val match = themeRegex.find(trimmed)!!
                val title = match.groupValues[3].trim()
                val populateThemeModel = themeList.find { it.title == title }
                val id = populateThemeModel?.id
                currentTheme = Theme(
                    id = id?.let { Uuid.parse(id) },
                    order = match.groupValues[1].trim(),
                    title = title
                )
                val themeOrder = populateThemeModel?.let { model ->
                    model.order.toString() + (model.suborder?.let { ".$it" } ?: "")
                }
                currentThemeWithImages = themeWithImagesString.find { it.themeOrder == themeOrder }
                themes += currentTheme!!
                currentQuestion = null
            }

            // QUESTION
            questionRegex.matches(trimmed) -> {
                val match = questionRegex.find(trimmed)!!
                val questionOrder = match.groupValues[1].toInt()
                var imageResId: String? = null
                if (currentThemeWithImages?.questionsWithImage?.contains(questionOrder) == true) {
                    val themeOrder = currentThemeWithImages?.themeOrder?.replace(".", "_")
                    imageResId = "image_t${themeOrder}_q${questionOrder.toString().padStart(3, '0')}"
                }
                val questionIdKey =  "${currentTheme?.order}_${questionOrder}"
                val questionId = questionIds[questionIdKey]
                currentQuestion = Question(
                    id = questionId ?: Uuid.random(),
                    order = questionOrder,
                    text = match.groupValues[2].trim(),
                    imageResId = imageResId
                )
                currentTheme?.questions?.add(currentQuestion!!)
            }

            // ANSWER
            answerRegex.matches(trimmed) -> {
                val match = answerRegex.find(trimmed)!!
                val themeId = currentTheme?.order
                val questionNumber = currentQuestion?.order
                val optionPosition = match.groupValues[1].toInt()
                val optionIdKey =  "${currentTheme?.order}_${currentQuestion?.order}_${optionPosition}"
                val optionId = questionOptionIds[optionIdKey]
                val answer = Answer(
                    id = optionId ?: Uuid.random(),
                    position = optionPosition,
                    text = match.groupValues[2].trim(),
                    isCorrect = answerMap[themeId]?.get(questionNumber) == optionPosition
                )
                currentQuestion?.answerOptionList?.add(answer)
            }
        }

    }
    val generatedData = themes.associate { theme ->
        val themeNumber = theme.order.replace(".", "_")
        val fileName = "questions_theme${themeNumber}.json"
        val questions = theme.questions.map { question ->
            PopulateQuestionModel(
                id = question.id.toString(),
                themeId = theme.id.toString(),
                order = question.order,
                text = question.text,
                imageResId = question.imageResId,
                answerOptionList = question.answerOptionList.map { answerOption ->
                    PopulateAnswerOptionModel(
                        id = answerOption.id.toString(),
                        position = answerOption.position,
                        text = answerOption.text,
                        isCorrect = answerOption.isCorrect
                    )
                }
            )
        }
        fileName to questions
    }
    val prettyPrintJson = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }
    generatedData.forEach { (fileName, questions) ->
        val questionsEncodedJson = prettyPrintJson.encodeToString(questions)
        File("src/main/resources/$fileName").writeText(questionsEncodedJson)
    }

}

fun parseQuestionsWithImages(input: String): List<ThemeImages> {
    val lines = input.trim().lines()
    val themeRegex = Regex("""^(\d+(.\d)?):(.*)$""")

    return lines.mapNotNull { line ->
        val match = themeRegex.matchEntire(line) ?: return@mapNotNull null

        val themeId = match.groupValues[1]
        val raw = match.groupValues[3].trim()

        if (raw.isEmpty()) {
            ThemeImages(themeId, emptyList())
        } else {
            val questions = raw.split(",")
                .flatMap { part ->
                    if (part.contains("-")) {
                        val (start, end) = part.split("-").map { it.toInt() }
                        (start..end).toList()
                    } else {
                        listOf(part.toInt())
                    }
                }

            ThemeImages(themeId, questions)
        }
    }
}

data class ThemeImages(
    val themeOrder: String,
    val questionsWithImage: List<Int>
)

data class Theme(
    val id: Uuid?,
    val order: String,
    val title: String,
    val questions: MutableList<Question> = mutableListOf()
)

data class Question(
    val id: Uuid,
    val order: Int,
    val text: String,
    val imageResId: String? = null,
    val answerOptionList: MutableList<Answer> = mutableListOf()
)

data class Answer(
    val id: Uuid,
    val position: Int,
    val text: String,
    val isCorrect: Boolean = false
)