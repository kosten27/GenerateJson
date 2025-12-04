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
    val sectionJsonString = File("src/main/resources/themes.json").readText()
    val sectionList = Json.decodeFromString<List<PopulateThemeModel>>(sectionJsonString)

    val questionIds = mutableMapOf<String, Uuid>()
    val questionOptionIds = mutableMapOf<String, Uuid>()
    val questionsSectionFiles = File("src/main/resources/questions_section_files.txt").readText().split("\n")
    questionsSectionFiles.forEach { questionsSectionFile ->
        val questionsSectionText = File("src/main/resources/$questionsSectionFile").readText()
        val questionList = Json.decodeFromString<List<PopulateQuestionModel>>(questionsSectionText)
        val currentSectionQuestionIds = questionList.associate { question ->
            val section = sectionList.find { section ->
                section.id == question.themeId
            }
            val sectionNumber = section?.let {
                it.order.toString() + it.suborder?.let { suborder -> ".$suborder" }.orEmpty()
            }
            "${sectionNumber}_${question.order}" to Uuid.parse(question.id)
        }
        questionIds.putAll(currentSectionQuestionIds)

        questionList.forEach { question ->
            val section = sectionList.find { section ->
                section.id == question.themeId
            }
            val sectionNumber = section?.let {
                it.order.toString() + it.suborder?.let { suborder -> ".$suborder" }.orEmpty()
            }
            val questionIdKey = "${sectionNumber}_${question.order}"
            val currentQuestionAnswerOptionIds = question.answerOptionList.associate { answerOption ->
                "${questionIdKey}_${answerOption.position}" to Uuid.parse(answerOption.id)
            }
            questionOptionIds.putAll(currentQuestionAnswerOptionIds)
        }
    }

    val sectionsWithImagesString = parseSectionsWithImages(File("src/main/resources/question_images.txt").readText())
    val answersString = File("src/main/resources/answers.txt").readText().split("\n")

    val answerSectionRegex = Regex("""^(\d*.\d*):(.+)""")
    val answerPairRegex = Regex("""(\d+)-(\d+)""")
    val answerMap: MutableMap<String, Map<Int, Int>> = mutableMapOf()
    answersString.forEach { line ->
        if (answerSectionRegex.matches(line)) {
            val matchResult = answerSectionRegex.find(line)!!
            val sectionId = matchResult.groupValues[1]
            val sectionAnswers = answerPairRegex.findAll(matchResult.groupValues[2]).map {
                val (a, b) = it.destructured
                a.toInt() to b.toInt()
            }.toMap()
            answerMap[sectionId] = sectionAnswers
        }
    }

    val sections = mutableListOf<Section>()
    var currentSection: Section? = null
    var currentSectionWithImages: SectionImages? = null
    var currentQuestion: Question? = null

    val sectionRegex = Regex("""^(\d+(.\d)?)\.\s+([A-ZА-ЯІЇЄҐ0-9\s'’\-(),]+)$""")
    val questionRegex = Regex("""^(\d+)\.\s+(.+)$""")
    val answerRegex = Regex("""^(\d+)\)\s+(.+)$""")

    val text = File("src/main/resources/pdr.txt").readText()
    text.lines().forEach {line ->
        val trimmed = line.trim()

        when {
            // SECTION
            sectionRegex.matches(trimmed) -> {
                val match = sectionRegex.find(trimmed)!!
                val title = match.groupValues[3].trim()
                val populateSectionModel = sectionList.find { it.title == title }
                val id = populateSectionModel?.id
                currentSection = Section(
                    id = id?.let { Uuid.parse(id) },
                    order = match.groupValues[1].trim(),
                    title = title
                )
                val sectionOrder = populateSectionModel?.let { model ->
                    model.order.toString() + (model.suborder?.let { ".$it" } ?: "")
                }
                currentSectionWithImages = sectionsWithImagesString.find { it.sectionOrder == sectionOrder }
                sections += currentSection!!
                currentQuestion = null
            }

            // QUESTION
            questionRegex.matches(trimmed) -> {
                val match = questionRegex.find(trimmed)!!
                val questionOrder = match.groupValues[1].toInt()
                var imageResId: String? = null
                if (currentSectionWithImages?.questionsWithImage?.contains(questionOrder) == true) {
                    val sectionOrder = currentSectionWithImages?.sectionOrder?.replace(".", "_")
                    imageResId = "image_s${sectionOrder}_q${questionOrder.toString().padStart(3, '0')}"
                }
                val questionIdKey =  "${currentSection?.order}_${questionOrder}"
                val questionId = questionIds[questionIdKey]
                currentQuestion = Question(
                    id = questionId ?: Uuid.random(),
                    order = questionOrder,
                    text = match.groupValues[2].trim(),
                    imageResId = imageResId
                )
                currentSection?.questions?.add(currentQuestion!!)
            }

            // ANSWER
            answerRegex.matches(trimmed) -> {
                val match = answerRegex.find(trimmed)!!
                val sectionId = currentSection?.order
                val questionNumber = currentQuestion?.order
                val optionPosition = match.groupValues[1].toInt()
                val optionIdKey =  "${currentSection?.order}_${currentQuestion?.order}_${optionPosition}"
                val optionId = questionOptionIds[optionIdKey]
                val answer = Answer(
                    id = optionId ?: Uuid.random(),
                    position = optionPosition,
                    text = match.groupValues[2].trim(),
                    isCorrect = answerMap[sectionId]?.get(questionNumber) == optionPosition
                )
                currentQuestion?.answerOptionList?.add(answer)
            }
        }

    }
    val generatedData = sections.associate { section ->
        val sectionNumber = section.order.replace(".", "_")
        val fileName = "questions_section${sectionNumber}.json"
        val questions = section.questions.map { question ->
            PopulateQuestionModel(
                id = question.id.toString(),
                themeId = section.id.toString(),
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

fun parseSectionsWithImages(input: String): List<SectionImages> {
    val lines = input.trim().lines()
    val sectionRegex = Regex("""^(\d+(.\d)?):(.*)$""")

    return lines.mapNotNull { line ->
        val match = sectionRegex.matchEntire(line) ?: return@mapNotNull null

        val sectionId = match.groupValues[1]
        val raw = match.groupValues[3].trim()

        if (raw.isEmpty()) {
            SectionImages(sectionId, emptyList())
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

            SectionImages(sectionId, questions)
        }
    }
}

data class SectionImages(
    val sectionOrder: String,
    val questionsWithImage: List<Int>
)

data class Section(
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