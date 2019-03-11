package dev.koiki.chord.communitysearch

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono

@Configuration
class CommunitySearchRouter(
        val handler: CommunitySearchHandler
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun routerFunction(): RouterFunction<ServerResponse> = router {
        POST("/search", handler::search)
    }
}
