package tech.uniapp.pdr.stage_test.domain.use_case

import kotlin.math.min

class SplitQuestionListIntoBatchesUseCase {
    operator fun <T> invoke(list: List<T>): List<List<T>> {
        val lastBatchQuestionCount = list.size % 10
        val lastBatchSize = (lastBatchQuestionCount + 10) / 2
        val oneBeforeLastBatchSize = 10 + lastBatchQuestionCount - lastBatchSize
        val result = (0..<list.size step 10)
            .map { index ->
                var indexFrom: Int = index
                var indexTo: Int = min(index + 9, list.size - 1)
                if (list.size < 10) {
                    indexTo = list.size - 1
                } else if (lastBatchQuestionCount == 0) {
                    indexTo = min(index + 9, list.size - 1)
                } else if (index == list.size - lastBatchQuestionCount - 10) {
                    indexTo = index + oneBeforeLastBatchSize - 1
                } else if (index == list.size - lastBatchQuestionCount) {
                    indexFrom = list.size - lastBatchSize
                    indexTo = list.size - 1
                }
                list.subList(indexFrom, indexTo + 1)
            }
        return result
    }
}