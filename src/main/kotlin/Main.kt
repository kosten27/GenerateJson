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

    val sections = mutableListOf<Section>()
    var currentSection: Section? = null
    var currentQuestion: Question? = null

    val sectionRegex = Regex("""^(\d+)\.\s+([A-ZА-ЯІЇЄҐ0-9\s'’\-(),]+)$""")
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
                val title = match.groupValues[2].trim()
                val id = sectionList.find { it.title == title }?.id
                currentSection = Section(
                    id = id?.let { Uuid.parse(id) },
                    order = match.groupValues[1].trim(),
                    title = title
                )
                sections += currentSection!!
                currentQuestion = null
            }

            // QUESTION
            questionRegex.matches(trimmed) -> {
                val match = questionRegex.find(trimmed)!!
                currentQuestion = Question(
                    id = Uuid.random(),
                    order = match.groupValues[1].toInt(),
                    text = match.groupValues[2].trim()
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