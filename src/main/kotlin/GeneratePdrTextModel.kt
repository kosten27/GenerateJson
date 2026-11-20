import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.builtins.StandardNames.FqNames.mutableList
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
    File("src/main/resources/pdr_text.txt").writeText(newPdrText)

    val paragraphs = newPdrText
        .trim()
        .split(Regex("(?m)^(?=\\d+\\.\\d+\\.\\s)"))
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val pdrHintModelList = mutableListOf<PopulatedPdrHintModel>()
    paragraphs.forEach { paragraph ->

        val pdrId = Regex("(?m)^\\d+\\.\\d+\\.\\s").find(paragraph)?.value?.trim()
        pdrId?.let {
            if (pdrId == "1.10.") {
                val paragraphs_1_10 =
                    paragraph
                        .trim()
                        .substringAfter("\n")
                        .split(Regex("""(?m)(?=^(?!-\s)[^\r\n]+?\s-\s)"""))
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                paragraphs_1_10.forEach { paragraph_1_10 ->
                    val pdr_1_10_id = Regex("""(?m)(?!-\s)[^\r\n]+?\s-\s""").find(paragraph_1_10)?.value?.trim()
                    pdr_1_10_id?.let {
                        val pdrIdWithPrefix = "1.10. " + pdr_1_10_id.replace(" -", "")
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
            } else {
                val id = pdrHintsIds.getOrDefault(pdrId, Uuid.random().toString())
                val pdrHintModel = PopulatedPdrHintModel(
                    id = id,
                    pdrId = pdrId,
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