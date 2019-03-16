package dev.koiki.chord.communitysearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.util.IOUtils
import org.springframework.http.ReactiveHttpOutputMessage
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

class ElasticsearchOperation {
    companion object {
        private val esClient = WebClient.create("http://localhost:9200")

        fun createIndex() {
            esClient.put()
                    .uri("/chord")
                    .body(readFile("schema.json"))
                    .exchange()
                    .block()
        }

        fun initializeDocuments() {
            val documents: List<Map<String, Any>> = ObjectMapper().readValue(
                    this::class.java.getResourceAsStream("/integrationtests/documents.json")
            )

            documents.parallelStream()
                    .forEach { document ->
                        esClient.post()
                                .uri("/chord/community")
                                .body(BodyInserters.fromObject(document))
                                .exchange()
                                .block()
                    }
        }

        fun dropIndex() {
            esClient.delete()
                    .uri("/chord")
                    .exchange()
                    .block()
        }

        private fun readFile(file: String): BodyInserter<String, ReactiveHttpOutputMessage> = BodyInserters.fromObject(
                IOUtils.toString(this::class.java.getResourceAsStream("/integrationtests/$file"))
        )
    }
}