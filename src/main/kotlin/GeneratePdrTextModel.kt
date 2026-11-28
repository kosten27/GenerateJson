import kotlinx.serialization.json.Json
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun main() {
    val pdrHintsString = File("src/main/resources/pdr_hints.json").readText()
    val pdrHintsList = Json.decodeFromString<List<PopulatedPdrHintModel>>(pdrHintsString)
    val pdrHintsIds: Map<String, String> = pdrHintsList.associate { pdrHint -> pdrHint.pdrId to pdrHint.id }

    val pdrText = File("src/main/resources/pdr_text.txt").readText()
    val newPdrText = pdrText
        .replace(Regex("(?m)^\\s*\\{.*}\\s*\\R?"), "") // Removed lines with {}
        .replace(Regex("\n{2,}"), "\n") // Removed empty lines
        .replace("“ ", "“")
        .replace("5.88. “", "5.88 “")
        .replace("6.10. “", "6.10 “")
    File("src/main/resources/pdr_text.txt").writeText(newPdrText)

    val paragraphs = newPdrText
        .trim()
        .split(Regex("(?m)^(?=\\d*\\.?\\d+\\.\\d+-?\\d*\\.\\s|\\d+\\.\\s\\p{Lu}[\\p{Lu}\\s]+\$)"))
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val pdrHintModelList = mutableListOf<PopulatedPdrHintModel>()
    paragraphs.forEach { paragraph ->
        val pdrId = Regex("(?m)^\\d*\\.?\\d*\\.?\\d+\\.\\s|\\d*\\.?\\d+\\-\\d+\\.\\s").find(paragraph)?.value?.trim()
        pdrId?.let {
            if (pdrId == "1.10.") {
                val paragraphs_1_10 =
                    paragraph
                        .trim()
                        .substringAfter("\n")
                        .split(Regex("""(?m)(?=^[а-яіїєґ][^\r\n]*?\s-\s)"""))
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                paragraphs_1_10.forEach { paragraph_1_10 ->
                    val pdr_1_10_id = Regex("""(?m)(?!-\s)[^\r\n]+?\s-\s""").find(paragraph_1_10)?.value?.trim()
                    pdr_1_10_id?.let {
                        val pdrIdWithPrefix = "1_10_" + pdr_1_10_id
                            .replace(" -", "")
                            .replace(" ", "_")
                            .replace("-", "_")
                            .replace("(", "")
                            .replace(")", "")
                            .replace(",", "")
                        val id = pdrHintsIds.getOrDefault(pdrIdWithPrefix, Uuid.random().toString())
                        val pdrHintModel = PopulatedPdrHintModel(
                            id = id,
                            pdrId = pdrIdWithPrefix,
                            value = paragraph_1_10.removePrefix(pdrId)
                                .trim().replaceFirstChar { it.uppercase() }
                                .trimEnd().let {
                                    if (it.endsWith(";")) it.dropLast(1) + "." else it
                                }
                        )
                        pdrHintModelList.add(pdrHintModel)
                    }
                }
            } else if (pdrId == "33.") {
                val paragraphs_33 =
                    paragraph
                        .trim()
                        .substringAfter("\n")
                        .split(Regex("(?m)^(?=\\d*\\.?\\d+\\.\\d+\\s(?!-)|\\d+\\.\\d+\\.\\d+-\\d+\\.\\d+\\.\\d+\\s|\\d+(?:\\.\\d+)+(?:,\\s*\\d+(?:\\.\\d+)+)*\\s(?!-))"))
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                paragraphs_33.forEach { paragraph_33 ->
                    val pdr_33_id = Regex("(?m)^(\\d+\\.\\d+\\.\\d+\\s+і\\s+\\d+\\.\\d+\\.\\d+\\s|\\d*\\.?\\d+\\.\\d+\\s(?!-)|\\d+\\.\\d+\\.\\d+-\\d+\\.\\d+\\.\\d+\\s|\\d+(?:\\.\\d+)+(?:,\\s*\\d+(?:\\.\\d+)+)*\\s(?!-))").find(paragraph_33)?.value?.trim()
                    pdr_33_id?.let {
                        val pdrIdWithPrefix = "rs_" + pdr_33_id
                            .replace(".", "_")
                            .replace(" ", "")
                            .replace("-", "_to_")
                            .replace("і", "_and_")
                            .replace(",", "_and_")
                        val id = pdrHintsIds.getOrDefault(pdrIdWithPrefix, Uuid.random().toString())
                        val firstLineParagraph = paragraph_33.substringBefore('\n').trim()
                        val pdrHintValue = if (firstLineParagraph.last() == ':')
                            (firstLineParagraph + "\n" + paragraph_33.substringAfter('\n').substringBefore(".\n"))
                                .trim()
                                .let {
                                    if (it.endsWith(".")) it else "$it."
                                }
                        else firstLineParagraph
                        val pdrHintModel = PopulatedPdrHintModel(
                            id = id,
                            pdrId = pdrIdWithPrefix,
                            value = pdrHintValue
                        )
                        pdrHintModelList.add(pdrHintModel)

                        if (pdrIdWithPrefix == "rs_1_41") { // End of 1. group
                            val pdrIdGroup1 = "rs_1_details"
                            val idGroup1 = pdrHintsIds.getOrDefault(pdrIdGroup1, Uuid.random().toString())
                            val pdrHintModelGroup1 = PopulatedPdrHintModel(
                                id = idGroup1,
                                pdrId = pdrIdGroup1,
                                value = paragraph_33.substringAfter('\n').substringBefore("\n2. Знаки пріоритету")
                            )
                            pdrHintModelList.add(pdrHintModelGroup1)
//                        } else if (pdrIdWithPrefix == "rs_2_6") { // End of 1. group
//                            val pdrIdGroup2 = "rs_2_details"
//                            val idGroup2 = pdrHintsIds.getOrDefault(pdrIdGroup2, Uuid.random().toString())
//                            val pdrHintModelGroup2 = PopulatedPdrHintModel(
//                                id = idGroup2,
//                                pdrId = pdrIdGroup2,
//                                value = paragraph_33.substringAfter('\n').substringBefore("\n3. Заборонні знаки")
//                            )
//                            pdrHintModelList.add(pdrHintModelGroup2)
                        } else if (pdrIdWithPrefix == "rs_3_45") { // End of 1. group
                            val pdrIdGroup3 = "rs_3_details"
                            val idGroup3 = pdrHintsIds.getOrDefault(pdrIdGroup3, Uuid.random().toString())
                            val pdrHintModelGroup3 = PopulatedPdrHintModel(
                                id = idGroup3,
                                pdrId = pdrIdGroup3,
                                value = paragraph_33.substringAfter('\n').substringBefore("\n4. Наказові знаки")
                            )
                            pdrHintModelList.add(pdrHintModelGroup3)
                        }
                    }
                }
            } else if (pdrId == "34.") {
                val paragraphs_34 =
                    paragraph
                        .trim()
                        .substringAfter("\n")
                        .split(Regex("(?m)^(?=\\d*\\.?\\d+\\.\\d+\\s|\\d+\\.\\d+\\.\\d+-\\d+\\.\\d+\\.\\d+\\s|\\d+(?:\\.\\d+)+(?:,\\s*\\d+(?:\\.\\d+)+)*)"))
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                paragraphs_34.forEach { paragraph_34 ->
                    val pdr_34_id = Regex("(?m)^(\\d*\\.?\\d+\\.\\d+\\s|\\d+\\.\\d+\\.\\d+-\\d+\\.\\d+\\.\\d+\\s|\\d+(?:\\.\\d+)+(?:,\\s*\\d+(?:\\.\\d+)+)*)").find(paragraph_34)?.value?.trim()
                    pdr_34_id?.let {
                        val pdrIdWithPrefix = "rm_" + pdr_34_id
                            .replace(".", "_")
                            .replace(" ", "")
                            .replace("-", "_to_")
                            .replace("і", "_and_")
                            .replace(",", "_and_")
                        val id = pdrHintsIds.getOrDefault(pdrIdWithPrefix, Uuid.random().toString())
                        val pdrHintModel = PopulatedPdrHintModel(
                            id = id,
                            pdrId = pdrIdWithPrefix,
                            value = "Розмітка " + paragraph_34.substringBefore('\n').trim()
                        )
                        pdrHintModelList.add(pdrHintModel)

                        if (pdrIdWithPrefix == "rm_1_40") { // End of 1. group
                            val pdrIdGroup1 = "rm_1_details"
                            val idGroup1 = pdrHintsIds.getOrDefault(pdrIdGroup1, Uuid.random().toString())
                            val pdrHintModelGroup1 = PopulatedPdrHintModel(
                                id = idGroup1,
                                pdrId = pdrIdGroup1,
                                value = paragraph_34.substringAfter('\n').substringBefore("\n2. Вертикальна розмітка")
                            )
                            pdrHintModelList.add(pdrHintModelGroup1)
                        }
                    }
                }
            } else if (pdrId.count { it == '.' } > 1) {
                val formattedPdrId = pdrId
                    .replace(".", "_")
                    .replace("-", "_0")
                    .dropLast(1)
                val id = pdrHintsIds.getOrDefault(formattedPdrId, Uuid.random().toString())
                val pdrHintModel = PopulatedPdrHintModel(
                    id = id,
                    pdrId = formattedPdrId,
                    value = paragraph.removePrefix(pdrId).trim()
                )
                pdrHintModelList.add(pdrHintModel)
            }
        }
    }

    val prettyPrintJson = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }
    val pdrHintModelListJson = prettyPrintJson.encodeToString(pdrHintModelList)
    File("src/main/resources/pdr_hints.json").writeText(pdrHintModelListJson)

    println("End")

}