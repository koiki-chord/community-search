package dev.koiki.chord.communitysearch

import org.junit.jupiter.api.Test

class LocalTestUtils {
    @Test
    fun init() {
        //ElasticsearchOperation.createIndex()
        ElasticsearchOperation.initializeDocuments()
    }

    //@Test
    fun drop() {
        ElasticsearchOperation.dropIndex()
    }
}