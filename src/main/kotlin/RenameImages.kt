import org.example.SectionImages
import java.io.File

fun main() {
    val imageNames = File("src/main/resources/image_names.txt").readText()
    val lines = imageNames.trim().lines()
    val sectionRegex = Regex("""^(\d+(.\d)?):(.*)$""")

    lines.forEach { line ->
        val match = sectionRegex.matchEntire(line) ?: return@forEach

        val sectionId = match.groupValues[1].replace(".", "_")
        val raw = match.groupValues[3].trim()

        if (raw.isEmpty()) {
            return@forEach
        } else {
            raw.split(",")
                .forEach { part ->
                    val (questionNumber, imageNumber) = part.split("-")
                    val originalImageName = "image_${imageNumber}.jpg"
                    val newImageName = "image_s${sectionId}_q${questionNumber.padStart(3, '0')}.jpg"
                    val bytes = File("src/main/resources/original_images/${originalImageName}").readBytes()
                    File("src/main/resources/renamed_images/${newImageName}").writeBytes(bytes)
                }

        }
    }
}