@file:OptIn(ExperimentalUuidApi::class)

package org.example

import PopulateSectionModel
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@OptIn(ExperimentalUuidApi::class)
fun main() {
//    println("Enter sectionId")
//    val sectionIdString = readln()
//    val sectionId = Uuid.parse(sectionIdString)
//    println("sectionId: $sectionId")

    val sectionsWithImages = parseSectionsWithImages(File("src/main/resources/question_images.txt").readText())

    val sections = mutableListOf<Section>()
    var currentSection: Section? = null
    var currentSectionWithImages: SectionImages? = null
    var currentQuestion: Question? = null

    val sectionRegex = Regex("""^(\d+(.\d)?)\.\s+([A-ZА-ЯІЇЄҐ0-9\s'’\-(),]+)$""")
    val questionRegex = Regex("""^(\d+)\.\s+(.+)$""")
    val answerRegex = Regex("""^(\d+)\)\s+(.+)$""")

    val sectionJsonString = File("src/main/resources/sections.json").readText()
    val sectionList = Json.decodeFromString<List<PopulateSectionModel>>(sectionJsonString)

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
                currentSectionWithImages = sectionsWithImages.find { it.sectionOrder == sectionOrder }
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
                    imageResId = "image_s${sectionOrder}_q${questionOrder}"
                }
                currentQuestion = Question(
                    id = Uuid.random(),
                    order = questionOrder,
                    text = match.groupValues[2].trim(),
                    imageResId = imageResId
                )
                currentSection?.questions?.add(currentQuestion!!)
            }

            // ANSWER
            answerRegex.matches(trimmed) -> {
                val match = answerRegex.find(trimmed)!!
                val answer = Answer(
                    id = Uuid.random(),
                    position = match.groupValues[1].toInt(),
                    text = match.groupValues[2].trim()
                )
                currentQuestion?.answerOptionList?.add(answer)
            }
        }

    }
    println(text.length)


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