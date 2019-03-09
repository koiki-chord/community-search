package dev.koiki.chord.communitysearch

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Component
class CommunitySearchHandler(
        val searchService: CommunitySearchService
) {
    fun findAll(request: ServerRequest): Mono<ServerResponse> = ServerResponse
            .ok()
            .body(searchService.findAll())
}
