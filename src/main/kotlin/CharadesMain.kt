import kotlinx.serialization.json.Json
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun main() {
    val words = File("src/main/resources/charades/words.txt").readText().split("\n")
    val populatedWords = words.map { word ->
        PopulatedWordModel(
            id = Uuid.random().toString(),
            value = word
        )
    }
    val populatedDeck = PopulatedDeckModel(
        words = populatedWords
    )

    val prettyPrintJson = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }
    val updatedThemeListEncodedJson = prettyPrintJson.encodeToString(populatedDeck)
    File("src/main/resources/charades/words.json").writeText(updatedThemeListEncodedJson)
}