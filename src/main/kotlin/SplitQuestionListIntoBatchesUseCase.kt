import kotlin.math.min

class SplitQuestionListIntoBatchesUseCase {
    operator fun <T> invoke(list: List<T>): List<List<T>> {
        val result = (0..<list.size step 10)
            .map { index ->
                var indexFrom: Int = index
                var indexTo: Int = min(index + 10, list.size)
                list.subList(indexFrom, indexTo)
            }
        return result
    }
}