package dev.koiki.chord.communitysearch

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class CommunitySearchRouter(
        val handler: CommunitySearchHandler
) {
    @Bean
    fun routerFunction(): RouterFunction<ServerResponse> = router {
        GET("/", handler::findAll)
        POST("/search", handler::search)
    }
}