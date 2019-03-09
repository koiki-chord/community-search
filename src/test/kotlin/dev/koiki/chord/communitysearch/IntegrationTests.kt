package dev.koiki.chord.communitysearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.util.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.ReactiveHttpOutputMessage
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTests(
        @LocalServerPort
        val port: Int
) {
    private lateinit var webClient: WebClient

    companion object {
        private val esClient = WebClient.create("http://localhost:9200")

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            esClient.put()
                    .uri("/chord")
                    .body(readFile("schema.json"))
                    .exchange()
                    .block()

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

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            esClient.delete()
                    .uri("/chord")
                    .exchange()
                    .block()
        }

        private fun readFile(file: String): BodyInserter<String, ReactiveHttpOutputMessage> = BodyInserters.fromObject(
                IOUtils.toString(this::class.java.getResourceAsStream("/integrationtests/$file"))
        )
    }

    @BeforeEach
    fun init() {
        webClient = WebClient.create("http://localhost:$port")
    }

    @Test
    fun test01() {
        val result: Mono<List<Community>> = webClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono()

        assertThat(result.block()!!.map { it.name }.toList())
                .contains("JJUG", "JSUG")
    }
}