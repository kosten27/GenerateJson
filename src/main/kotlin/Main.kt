@file:OptIn(ExperimentalUuidApi::class)

package org.example

import PopulateAnswerOptionModel
import PopulateQuestionModel
import PopulateStageModel
import PopulateThemeModel
import PopulatedQuestionRange
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.min
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@OptIn(ExperimentalUuidApi::class, ExperimentalSerializationApi::class)
fun main() {
    val themeJsonString = File("src/main/resources/themes.json").readText()
    val themeList = Json.decodeFromString<List<PopulateThemeModel>>(themeJsonString)

    val stageIds = mutableMapOf<String, Uuid>()
    val themeStageFiles = File("src/main/resources/stages_theme_files.txt").readText().split("\n")
    themeStageFiles.forEach { themeStageFile ->
        val themeStageText = File("src/main/resources/$themeStageFile").readText()
        val stageList = Json.decodeFromString<List<PopulateStageModel>>(themeStageText)
        val currentThemeStageIds = stageList.associate { stage ->
            val theme = themeList.find { theme ->
                theme.id == stage.themeId
            }
            val themeNumber = theme?.let {
                it.order.toString() + it.suborder?.let { suborder -> ".$suborder" }.orEmpty()
            }
            "${themeNumber}_${stage.order}" to Uuid.parse(stage.id)
        }
        stageIds.putAll(currentThemeStageIds)

    }

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
    var currentTheme33Map: Map<String, Theme>? = null
    var currentThemeWithImages: ThemeImages? = null
    var currentQuestion: Question? = null

    val themeRegex = Regex("""^(\d+(.\d)?)\.\s+([A-ZА-ЯІЇЄҐ0-9\s'’\-(),]+)$""")
    val questionRegex = Regex("""^(\d+)\.\s+(.+)$""")
    val answerRegex = Regex("""^(\d+)\)\s+(.+)$""")

    val matchTheme33 = File("src/main/resources/match_theme_33.txt").readText()
    val parsedMatchTheme33 = parseMatchTheme(matchTheme33)

    val text = File("src/main/resources/pdr.txt").readText()
    text.lines().forEach { line ->
        val trimmed = line.trim()

        when {
            // THEME
            "33. ДОРОЖНІ ЗНАКИ" == trimmed -> {
                currentTheme33Map = parsedMatchTheme33.keys.sorted().associate { themeOrder ->
                    val populateThemeModel = themeList.find { "${it.order}.${it.suborder}" == themeOrder }
                    val id = populateThemeModel?.id
                    themeOrder to Theme(
                        id = id?.let { Uuid.parse(id) },
                        order = themeOrder,
                        title = ""
                    )
                }
                currentThemeWithImages = themeWithImagesString.find { it.themeOrder == "33" }
                currentTheme33Map?.let { themes.addAll(it.values) }
                currentQuestion = null
                val match = themeRegex.find(trimmed)!!
                val title = match.groupValues[3].trim()
                val populateThemeModel = themeList.find { it.title == title }
                val id = populateThemeModel?.id
                currentTheme = Theme(
                    id = id?.let { Uuid.parse(id) },
                    order = match.groupValues[1].trim(),
                    title = title
                )
            }
            themeRegex.matches(trimmed) -> {
                val match = themeRegex.find(trimmed)!!
                val splitOrder = match.groupValues[1].trim().split(".")
                val order = splitOrder.first().toInt()
                val suborder = if (splitOrder.size > 1) splitOrder[1].toInt() else null
                val title = match.groupValues[3].trim()
                val populateThemeModel = themeList.find { it.order == order && it.suborder == suborder  }
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
                currentTheme33Map = null
            }

            // QUESTION
            questionRegex.matches(trimmed) -> {
                val match = questionRegex.find(trimmed)!!
                val sourceQuestionOrder = match.groupValues[1].toInt()
                var imageResId: String? = null
                if (currentThemeWithImages?.questionsWithImage?.contains(sourceQuestionOrder) == true) {
                    val themeOrder = currentThemeWithImages?.themeOrder?.replace(".", "_")
                    imageResId = "image_t${themeOrder}_q${sourceQuestionOrder.toString().padStart(3, '0')}"
                }
                val questionTheme = if (currentTheme33Map == null) currentTheme else {
                    val questionThemeOrder = parsedMatchTheme33.filter { it.value.contains(sourceQuestionOrder) }.map { it.key }.first()
                    currentTheme33Map?.get(questionThemeOrder)
                }
                val currentQuestionOrder = if (currentTheme33Map == null) sourceQuestionOrder else questionTheme?.questions?.size?.plus(1) ?: 1
                val questionIdKey =  "${questionTheme?.order}_${sourceQuestionOrder}"
                val questionId = questionIds[questionIdKey]
                currentQuestion = Question(
                    id = questionId ?: Uuid.random(),
                    sourceOrder = sourceQuestionOrder,
                    order = currentQuestionOrder,
                    text = match.groupValues[2].trim(),
                    imageResId = imageResId
                )
                questionTheme?.questions?.add(currentQuestion!!)
            }

            // ANSWER
            answerRegex.matches(trimmed) -> {
                val match = answerRegex.find(trimmed)!!
                val sourceThemeId = currentTheme?.order
                val questionNumber = currentQuestion?.sourceOrder
                val answerTheme = if (currentTheme33Map == null) currentTheme else {
                    val questionThemeOrder = parsedMatchTheme33.filter { it.value.contains(questionNumber) }.map { it.key }.first()
                    currentTheme33Map?.get(questionThemeOrder)
                }
                val optionPosition = match.groupValues[1].toInt()
                val optionIdKey =  "${answerTheme?.order}_${currentQuestion?.sourceOrder}_${optionPosition}"
                val optionId = questionOptionIds[optionIdKey]
                val answer = Answer(
                    id = optionId ?: Uuid.random(),
                    position = optionPosition,
                    text = match.groupValues[2].trim(),
                    isCorrect = answerMap[sourceThemeId]?.get(questionNumber) == optionPosition
                )
                currentQuestion?.answerOptionList?.add(answer)
            }
        }

    }
    val generatedQuestionData = themes.associate { theme ->
        val themeNumber = theme.order.replace(".", "_")
        val fileName = "questions_theme${themeNumber}.json"
        val questions = theme.questions.map { question ->
            PopulateQuestionModel(
                id = question.id.toString(),
                themeId = theme.id.toString(),
                sourceOrder = question.sourceOrder,
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
    generatedQuestionData.forEach { (fileName, questions) ->
        val questionsEncodedJson = prettyPrintJson.encodeToString(questions)
        File("src/main/resources/$fileName").writeText(questionsEncodedJson)
    }

    val generatedStageData = themes.associate { theme ->
        val themeNumber = theme.order.replace(".", "_")
        val fileName = "stages_theme${themeNumber}.json"
        val lastQuestionOrder = theme.questions.last().order
        val questionNumberInLastStage = lastQuestionOrder % 10
        val stages = (1..lastQuestionOrder step 10)
            .filter { i -> questionNumberInLastStage >= 5 || i < lastQuestionOrder - questionNumberInLastStage || lastQuestionOrder < 5}
            .mapIndexed { index, i ->
//            if (questionNumberInLastStage < 5 && i > lastQuestionOrder - questionNumberInLastStage) {
//                return@mapIndexed
//            }

            val questionRangeTo = if (questionNumberInLastStage < 5 && (i + 9) == (lastQuestionOrder - questionNumberInLastStage)) { // one stage before last
                lastQuestionOrder
            } else {
                min(i + 9, lastQuestionOrder)
            }
            val stageOrder = index + 1
            val stageIdKey =  "${theme.order}_${stageOrder}"
            val stageId = stageIds[stageIdKey] ?: Uuid.random()
            PopulateStageModel(
                id = stageId.toString(),
                themeId = theme.id.toString(),
                order = stageOrder,
                type = PopulateStageType.REGULAR,
                questionRange = PopulatedQuestionRange(
                    from = i,
                    to = questionRangeTo
                ),
            )
        }
        val stageOrder = stages.size + 1
        val stageIdKey =  "${theme.order}_${stageOrder}"
        val stageId = stageIds[stageIdKey] ?: Uuid.random()
        val mistakesReviewStage = PopulateStageModel(
            id = stageId.toString(),
            themeId = theme.id.toString(),
            order = stageOrder,
            type = PopulateStageType.MISTAKES_REVIEW,
            questionRange = null
        )
        fileName to (stages + mistakesReviewStage)
    }

    generatedStageData.forEach { (fileName, questions) ->
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

fun parseMatchTheme(input: String): Map<String, List<Int>> {
    val lines = input.trim().lines()
    val themeRegex = Regex("""^(\d+(.\d)?):(.*)$""")

    return lines.associate { line ->
        val match = themeRegex.matchEntire(line)!!

        val subThemeId = match.groupValues[1]
        val raw = match.groupValues[3].trim()

        val questions = raw.split(",")
            .flatMap { part ->
                if (part.contains("-")) {
                    val (start, end) = part.split("-").map { it.toInt() }
                    (start..end).toList()
                } else {
                    listOf(part.toInt())
                }
            }
        subThemeId to questions
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

data class QuestionRange(
    val from: Int,
    val to: Int
)

data class Question(
    val id: Uuid,
    val sourceOrder: Int,
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