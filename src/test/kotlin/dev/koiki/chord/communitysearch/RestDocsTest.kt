package dev.koiki.chord.communitysearch

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse

@ExtendWith(SpringExtension::class, RestDocumentationExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestDocsTest {
    private lateinit var webTestClient: WebTestClient
    private lateinit var myHandler: MyHandler

    @BeforeEach
    fun setup(restDocumentation: RestDocumentationContextProvider) {
        myHandler = mock(MyHandler::class.java)
        val myRouter = MyRouter(myHandler)

        this.webTestClient = WebTestClient.bindToRouterFunction(myRouter.routerFunction())
                .configureClient()
                .filter(documentationConfiguration(restDocumentation))
                .build()
    }

    // any() of mockito will throw exception in Kotlin
    // this magic code prevents it
    private fun <T> any(): T {
        Mockito.any<T>()
        return uninitialized()
    }
    private fun <T> uninitialized(): T = null as T

    @Test
    fun search() {
        val expected = BodyInserters.fromObject(listOf(
                Community(name = "JSUG", desc = "Japan Spring User Group", tags = listOf(Tag(value = "Java")))
        ))

        `when`(myHandler.search(any()))
                .thenReturn(ServerResponse.ok().body(expected))

        this.webTestClient
                .get()
                .uri("/search?text={text}&size={size}&page={page}", mapOf("text" to "Java Spring", "size" to 20, "page" to 0))
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .consumeWith(document("search",
                        requestParameters(
                                parameterWithName("text")
                                        .description("""NotNull and NotBlank.
                                            | Text for full-text search.
                                            | Space represents "AND" condition
                                            | so if text is "Java Spring", search result must have "Java" and "Spring".""".trimMargin()),
                                parameterWithName("size")
                                        .description("Default is 20. TODO"),
                                parameterWithName("page")
                                        .description("Default is 0. TODO")
                        ),
                        responseFields(
                                fieldWithPath("[].name").description("Name"),
                                fieldWithPath("[].desc").description("Description"),
                                fieldWithPath("[].tags[].value").description("Tag value such as 'Java'")
                        )
                ))
    }

    @Test
    fun complete() {
        val expected = BodyInserters.fromObject(listOf(
                CommunityComplete(text = "Basketball", score = 1.0f),
                CommunityComplete(text = "Baseball", score = 0.89f)
        ))

        `when`(myHandler.complete(any()))
                .thenReturn(ServerResponse.ok().body(expected))

        this.webTestClient
                .get()
                .uri("/complete?keyword={keyword}", mapOf("keyword" to "b"))
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .consumeWith(document("complete",
                        requestParameters(
                                parameterWithName("keyword")
                                        .description("""NotNull and NotEmpty.
                                            | Keyword for completion suggestion.""".trimMargin())
                        ),
                        responseFields(
                                fieldWithPath("[].text").description("Suggested completion keyword"),
                                fieldWithPath("[].score").description("Score, this is float value")
                        )
                ))
    }
}