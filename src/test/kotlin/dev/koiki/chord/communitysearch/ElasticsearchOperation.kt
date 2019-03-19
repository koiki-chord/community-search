package dev.koiki.chord.communitysearch

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.util.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.http.ReactiveHttpOutputMessage
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

class ElasticsearchOperation {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
        private val esClient = WebClient.create("http://localhost:9200")

        fun createIndex() {
            esClient.put()
                    .uri("/chord")
                    .header("Content-Type", "application/json")
                    .body(readFile("schema.json"))
                    .retrieve()
                    .bodyToMono<JsonNode>()
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
                                .header("Content-Type", "application/json")
                                .body(BodyInserters.fromObject(document))
                                .retrieve()
                                .bodyToMono<JsonNode>()
                                .block()
                    }
        }

        fun dropIndex() {
            esClient.delete()
                    .uri("/chord")
                    .retrieve()
                    .bodyToMono<JsonNode>()
                    .block()
        }

        private fun readFile(file: String): BodyInserter<String, ReactiveHttpOutputMessage> = BodyInserters.fromObject(
                IOUtils.toString(this::class.java.getResourceAsStream("/integrationtests/$file"))
        )
    }
}