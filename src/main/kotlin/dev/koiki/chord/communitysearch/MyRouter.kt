package dev.koiki.chord.communitysearch

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class MyRouter(val handler: MyHandler) {

    @Bean
    fun routerFunction(): RouterFunction<ServerResponse> = router {
        GET("/search", handler::search)
        GET("/complete", handler::complete)
    }
}
