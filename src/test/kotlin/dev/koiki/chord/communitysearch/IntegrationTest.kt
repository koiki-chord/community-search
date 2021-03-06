package dev.koiki.chord.communitysearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import dev.koiki.chord.communitysearch.search.CommunitySearchResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

/**
 * This test will connect to actual Elasticsearch instance on your local.
 *
 * !! WARNING !!
 * This will initialize "chord" index every time test runs.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest(
        @LocalServerPort
        private val port: Int
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val mapper = ObjectMapper().registerModule(KotlinModule())

    private lateinit var webClient: WebClient

    companion object {
        private val skipInit = false
        private val skipDestroy = false

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            if (skipInit)
                return

            ElasticsearchOperation.createIndex()
            ElasticsearchOperation.initializeDocuments()
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            if (skipDestroy)
                return

            ElasticsearchOperation.dropIndex()
        }
    }

    @BeforeEach
    fun init() {
        webClient = WebClient.create("http://localhost:$port")
    }

    @Test
    fun search01() {
        val resultMono: Mono<CommunitySearchResult> = webClient.get()
                .uri("/search?text={text}", mapOf("text" to "CoD BO"))
                .retrieve()
                .bodyToMono()

        val result = resultMono.block()

        assertThat(result!!.communities)
                .hasSize(2)
    }

    @Test
    fun complete01() {
        val resultMono: Mono<List<CommunityComplete>> = webClient.get()
                .uri("/complete?keyword={keyword}", mapOf("keyword" to "c"))
                .retrieve()
                .bodyToMono()

        val result = resultMono.block()

        assertThat(result)
                .isEqualTo(listOf(CommunityComplete("CoD BOシリーズ", 1.0f)))
    }

    //@Test TODO
    fun validationError01() {
        val resultMono: Mono<Any> = webClient.post()
                .uri("/search")
                .retrieve()
                .bodyToMono()

        try {
            resultMono.block()

            fail("WebClientResponseException should be thrown")
        } catch (e: WebClientResponseException) {
            val errorRes: ErrorResponse = mapper.readValue(e.responseBodyAsString)
            assertThat(errorRes.code).isEqualTo(ErrorCode.TEXT_SHOULD_NOT_BE_BLANK)
        }
    }
}