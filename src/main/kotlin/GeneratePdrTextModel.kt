import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.builtins.StandardNames.FqNames.mutableList
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun main() {
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
            val pdrHintModel = PopulatedPdrHintModel(
                id = Uuid.random().toString(),
                pdrId = pdrId,
                value = paragraph.removePrefix(pdrId).trim()
            )
            pdrHintModelList.add(pdrHintModel)
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